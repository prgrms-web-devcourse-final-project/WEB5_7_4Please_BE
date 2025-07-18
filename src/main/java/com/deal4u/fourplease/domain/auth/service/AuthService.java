package com.deal4u.fourplease.domain.auth.service;

import com.deal4u.fourplease.domain.auth.dto.GoogleTokenResponse;
import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.entity.BlacklistedToken;
import com.deal4u.fourplease.domain.auth.entity.RefreshToken;
import com.deal4u.fourplease.domain.auth.property.GoogleOauthProperties;
import com.deal4u.fourplease.domain.auth.repository.BlacklistedTokenRepository;
import com.deal4u.fourplease.domain.auth.repository.RefreshTokenRepository;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final GoogleOauthProperties googleOauthProperties;
    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;

    // 로그인 시 토큰 생성 및 저장
    public TokenPair createTokenPair(Member member) {
        TokenPair tokenPair = jwtProvider.generateTokenPair(member);

        LocalDateTime expiryDate = jwtProvider.getExpirationFromToken(tokenPair.refreshToken());
        refreshTokenRepository.findByMember(member)
                .ifPresentOrElse(
                        existing -> existing.updateToken(tokenPair.refreshToken(), expiryDate),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .member(member)
                                        .token(tokenPair.refreshToken())
                                        .expiryDate(expiryDate)
                                        .build()
                        )
                );
        return tokenPair;
    }

    // Authorization 헤더 검증 로직
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw ErrorCode.INVALID_AUTH_HEADER.toException();
        }

        String token = authHeader.replace("Bearer ", "");
        if (token.isBlank()) {
            throw ErrorCode.INVALID_AUTH_HEADER.toException();
        }

        return token;
    }

    @Transactional
    public ResponseEntity<Void> logout(String authHeader, Member member) {
        // Authorization 헤더에서 토큰 추출 및 검증
        String token = extractTokenFromHeader(authHeader);

        // 토큰 유효성 검사
        jwtProvider.validateOrThrow(token);

        // 이미 블랙리스트에 있는지 확인
        if (blacklistedTokenRepository.existsByToken(token)) {
            throw ErrorCode.TOKEN_ALREADY_BLACKLISTED.toException();
        }

        // 액세스 토큰을 블랙리스트에 추가
        LocalDateTime expiration = jwtProvider.getExpirationFromToken(token);
        blacklistedTokenRepository.save(
                BlacklistedToken.builder()
                        .token(token)
                        .expiryDate(expiration)
                        .build()
        );
        log.info("Access token이 블랙리스트에 추가됨: {}", member.getEmail());

        // 리프레시 토큰 삭제
        refreshTokenRepository.deleteByMember(member);
        log.info("사용자 로그아웃 완료: {}", member.getEmail());
        return ResponseEntity.noContent().build();
    }

    // 리프레시 토큰을 통해 새로운 액세스토큰과 리프레시 토큰을 재발급
    public TokenPair refreshAccessToken(String refreshToken) {
        // 토큰 유효성 검사
        jwtProvider.validateOrThrow(refreshToken);

        // 토큰 타입 확인 (refresh인지 확인)
        String tokenType = jwtProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw ErrorCode.INVALID_TOKEN_TYPE.toException();
        }

        // DB에 저장된 토큰인지 확인
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(ErrorCode.INVALID_REFRESH_TOKEN::toException);

        // DB 기준으로 만료 여부 확인
        if (savedToken.isExpired()) {
            refreshTokenRepository.delete(savedToken); // 만료된 토큰 삭제
            throw ErrorCode.TOKEN_EXPIRED.toException();
        }
        // 검증된 토큰을 가진 유저이므로 새로운 토큰 생성
        Member member = savedToken.getMember();
        return createTokenPair(member);
    }

    public ResponseEntity<Void> deactivateMember(String authHeader, Member member) {
        // Authorization 헤더 검증
        extractTokenFromHeader(authHeader);

        // 서버 토큰 블랙리스트 처리
        // logout(authHeader, member);

        // 소셜 accessToken 발급
        String accessToken = refreshGoogleAccessToken(member.getRefreshToken());

        // 소셜 연동 해제
        unlinkSocialAccount(member, accessToken);

        member.setRefreshToken(null);
        member.setStatus(Status.DELETED);
        memberRepository.save(member);

        // 서버의 refresh token 폐기
        refreshTokenRepository.deleteByMember(member);

        log.info("회원 탈퇴 완료: {}", member.getEmail());
        return ResponseEntity.noContent().build();
    }

    // refresh 토큰을 통해 AccessToken 재발급
    // 이때 accessToken 재발급에 실패하면 refreshToken도 재발급 해야함
    // -> 컨트롤러를 통해 프론트에게 access_type=offline + prompt=consent 붙여서 요청
    public String refreshGoogleAccessToken(String refreshToken) {
        log.info("소셜 리프레시 토큰을 통해 액세스 토큰을 재발급 메서드 진입");
        String url = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", googleOauthProperties.getClientId());
        params.add("client_secret", googleOauthProperties.getClientSecret());
        params.add("refresh_token", refreshToken);
        params.add("grant_type", "refresh_token");
        log.info("Google 토큰 갱신 요청 파라미터: {}", params);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    GoogleTokenResponse.class
            );
            log.info("GoogleTokenResponse 확인: {}", response);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("is2xxSuccessful 진입 확인");
                return response.getBody().accessToken();
            } else {
                log.error("구글 accesstoken 재발급 실패. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
                // accesstoken에 재발급에 실패했으므로 refreshtoken이 만료
                // errorcode 던져주면 이걸로 refresh 토큰 재발급 controller 호출
                // 반드시 재발급이 가능한 에러 코드로 이어져야함
                throw ErrorCode.SOCIAL_REFRESH_TOKEN_EXPIRED.toException();
            }
        } catch (RestClientException e) {
            throw ErrorCode.SOCIAL_UNLINK_FAILED.toException();
        }

    }

    private void unlinkSocialAccount(Member member, String accessToken) {
        String provider = member.getProvider();
        log.info("소셜 연동 해제 try: {}", provider);
        try {
            switch (provider) {
                case "google" -> unlinkGoogle(member, accessToken);
                case "kakao" -> log.info("아직 미구현");
                default -> log.warn("알 수 없는 provider: {}", provider);
            }
        } catch (Exception e) {
            log.error("연동 해제 실패: {}", e.getMessage());
            throw ErrorCode.SOCIAL_UNLINK_FAILED.toException();
        }

    }

    private void unlinkGoogle(Member member, String accessToken) {
        try {
            String url = "https://oauth2.googleapis.com/revoke";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("token", accessToken);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("구글 연동 해제 성공: {}", member.getEmail());
            } else {
                log.warn("구글 연동 해제 실패: {}, 응답: {}", member.getEmail(), response);
            }

        } catch (Exception e) {
            log.warn("구글 연동 해제 요청 실패: {}", member.getEmail(), e);
        }
    }
}
