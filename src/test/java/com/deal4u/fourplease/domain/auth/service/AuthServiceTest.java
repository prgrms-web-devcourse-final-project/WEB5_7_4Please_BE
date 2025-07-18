package com.deal4u.fourplease.domain.auth.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.entity.RefreshToken;
import com.deal4u.fourplease.domain.auth.repository.BlacklistedTokenRepository;
import com.deal4u.fourplease.domain.auth.repository.RefreshTokenRepository;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private final String validToken = "mocked.jwt.token";
    private final String email = "test@example.com";
    @InjectMocks
    private AuthService authService;

    @InjectMocks
    private LogoutService logoutService;

    @Mock
    private Member member;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshToken refreshToken;
    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;
    private TokenPair tokenPair;

    @BeforeEach
    void setUp() {
        tokenPair = new TokenPair("access", "refresh");
    }

    @Test
    @DisplayName("기존 리프레시 토큰이 존재하면 업데이트하고 토큰 반환")
    void createTokenPair_existingToken_updatesTokenAndReturnsPair() {
        // given
        when(jwtProvider.generateTokenPair(member)).thenReturn(tokenPair);
        when(refreshTokenRepository.findByMember(member)).thenReturn(Optional.of(refreshToken));
        when(jwtProvider.getExpirationFromToken("refresh"))
                .thenReturn(LocalDateTime.now().plusDays(7));

        // when
        TokenPair result = authService.createTokenPair(member);

        // then
        verify(refreshToken).updateToken(eq("refresh"), any(LocalDateTime.class));
        assertThat(result).isEqualTo(tokenPair);
    }

    @Test
    @DisplayName("기존 리프레시 토큰이 없으면 새로 저장하고 토큰 반환")
    void createTokenPair_noToken_savesNewRefreshToken() {
        // given
        when(jwtProvider.generateTokenPair(member)).thenReturn(tokenPair);
        when(refreshTokenRepository.findByMember(member)).thenReturn(Optional.empty());

        // when
        TokenPair result = authService.createTokenPair(member);

        // then
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertThat(result).isEqualTo(tokenPair);
    }

    @Test
    @DisplayName("유효한 리프레시 토큰일 때 새 토큰 반환")
    void refreshAccessToken_validToken_returnsNewTokenPair() {
        // given
        String validRefresh = "valid-refresh-token";

        when(jwtProvider.getTokenType(validRefresh)).thenReturn("refresh");
        when(refreshTokenRepository.findByToken(validRefresh)).thenReturn(
                Optional.of(refreshToken));
        when(refreshToken.isExpired()).thenReturn(false);
        when(refreshToken.getMember()).thenReturn(member);
        when(jwtProvider.generateTokenPair(member)).thenReturn(tokenPair);

        // when
        TokenPair result = authService.refreshAccessToken(validRefresh);

        // then
        assertThat(result).isEqualTo(tokenPair);
    }

    @Test
    @DisplayName("토큰 타입이 refresh가 아니면 예외 발생")
    void refreshAccessToken_invalidTokenType_throwsException() {
        // given
        String wrongRefreshToken = "wrong-type-token";
        when(jwtProvider.getTokenType(wrongRefreshToken)).thenReturn("access");

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(wrongRefreshToken))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.INVALID_TOKEN_TYPE.getMessage())
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("DB에 저장되지 않은 리프레시 토큰일 경우 예외 발생")
    void refreshAccessToken_tokenNotFound_throwsException() {
        // given
        String unknownRefreshToken = "unknown-token";
        when(jwtProvider.getTokenType(unknownRefreshToken)).thenReturn("refresh");
        when(refreshTokenRepository.findByToken(unknownRefreshToken)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(unknownRefreshToken))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.INVALID_REFRESH_TOKEN.getMessage())
                .extracting("status")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("만료된 리프레시 토큰일 경우 삭제 후 예외 발생")
    void refreshAccessToken_expiredToken_deletesAndThrows() {
        // given
        String expiredRefreshToken = "expired-token";
        when(jwtProvider.getTokenType(expiredRefreshToken)).thenReturn("refresh");
        when(refreshTokenRepository.findByToken(expiredRefreshToken)).thenReturn(
                Optional.of(refreshToken));
        when(refreshToken.isExpired()).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(expiredRefreshToken))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.TOKEN_EXPIRED.getMessage())
                .extracting("status")
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    @DisplayName("로그 아웃 성공 시 블랙리스트 등록 및 리프레시 토큰 삭제 처리")
    void logout_success() {
        String authHeader = "Bearer " + validToken;
        Member newMember = Member.builder()
                .email(email)
                .status(Status.ACTIVE)
                .build();

        when(jwtProvider.getExpirationFromToken(validToken)).thenReturn(
                LocalDateTime.now().plusMinutes(15));
        when(blacklistedTokenRepository.existsByToken(validToken)).thenReturn(false);
        doNothing().when(jwtProvider).validateOrThrow(validToken);

        ResponseEntity<Void> response = logoutService.logout(authHeader, newMember);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(blacklistedTokenRepository).save(org.mockito.ArgumentMatchers.any());
        verify(refreshTokenRepository).deleteByMember(newMember);
    }

    @Test
    @DisplayName("이미 블랙리스트에 등록된 토큰으로 로그아웃 시 예외 발생")
    void logout_shouldFailIfTokenAlreadyBlacklisted() {
        String authHeader = "Bearer " + validToken;
        Member newMember = Member.builder()
                .email(email)
                .status(Status.ACTIVE)
                .build();

        when(blacklistedTokenRepository.existsByToken(validToken)).thenReturn(true);
        doNothing().when(jwtProvider).validateOrThrow(validToken);

        assertThatThrownBy(() -> logoutService.logout(authHeader, newMember))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.TOKEN_ALREADY_BLACKLISTED.getMessage());
    }
}
