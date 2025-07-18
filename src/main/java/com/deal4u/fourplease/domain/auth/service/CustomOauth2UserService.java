package com.deal4u.fourplease.domain.auth.service;

import com.deal4u.fourplease.domain.auth.model.Customoauth2User;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;
    private final AuthService authService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        log.info("CustomOAuth2UserService.loadUser 진입");
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(request); // Spring이 가져온 사용자 정보
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String provider = request.getClientRegistration().getRegistrationId();

        if (email == null || email.isBlank()) {
            throw ErrorCode.OAUTH_EMAIL_NOT_FOUND.toException();
        }
        // 소셜 refreshToken 발급
        // 최초 로그인이거나 재동의 후 로그인이면 값이 존재
        String refreshToken = (String) request.getAdditionalParameters().get("refresh_token");
        log.info("소셜 리프레시 토큰 발급 = {}", refreshToken);

        // 1. DB에 해당 이메일이 있는지 확인
        Optional<Member> optionalMember = memberRepository.findByEmailAndProvider(email, provider);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            // member deleted면 로그인 불가
            if (member.getStatus() == Status.DELETED) {
                throw ErrorCode.MEMBER_WITHDRAWN.toException();
            }

            if (refreshToken != null && !refreshToken.isBlank()
                    && !refreshToken.equals(member.getRefreshToken())) {
                member.setRefreshToken(refreshToken);
                memberRepository.save(member);
                log.info("재동의 후 새 refresh token 갱신 완료");
            }

            // 소셜 accesstoken 재발급 성공 시 → 새 소셜 refreshToken 필요 없음
            String accessToken =
                    authService.refreshGoogleAccessToken(member.getRefreshToken());
            log.info("기존 소셜 refresh token 유효함: access token = {}", accessToken);

            return new Customoauth2User(member, attributes);
        }

        // 2. 없다면 생성 (최초 로그인)
        Member newMember = memberRepository.save(
                Member.builder()
                        .email(email)
                        .provider("google")
                        .refreshToken(refreshToken)
                        .role(Role.USER)
                        .status(Status.PENDING)
                        .build());
        log.info("신규 회원 저장됨: " + newMember.getMemberId());
        return new Customoauth2User(newMember, attributes);

    }
}
