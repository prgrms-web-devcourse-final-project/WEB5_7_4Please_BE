package com.deal4u.fourplease.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auth.entity.BlacklistedToken;
import com.deal4u.fourplease.domain.auth.repository.BlacklistedTokenRepository;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {
    @InjectMocks
    private LogoutService logoutService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    private String refreshToken;

    @BeforeEach
    void setUp() {
        refreshToken = "mocked.refresh.token";
    }

    @Test
    @DisplayName("유효한 리프레시 토큰이면 블랙리스트에 등록 성공")
    void logout_validRefreshToken_blacklistAndReturnNoContent() {
        // given
        LocalDateTime expiration = LocalDateTime.now().plusDays(7);
        doNothing().when(jwtProvider).validateOrThrow(refreshToken);
        when(jwtProvider.getTokenType(refreshToken)).thenReturn("refresh");
        when(blacklistedTokenRepository.existsByToken(refreshToken)).thenReturn(false);
        when(jwtProvider.getExpirationFromToken(refreshToken)).thenReturn(expiration);

        // when
        ResponseEntity<Void> response = logoutService.logout(refreshToken);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        ArgumentCaptor<BlacklistedToken> captor = ArgumentCaptor.forClass(BlacklistedToken.class);
        verify(blacklistedTokenRepository).save(captor.capture());

        assertThat(captor.getValue().getToken()).isEqualTo(refreshToken);
        assertThat(captor.getValue().getExpiryDate()).isEqualTo(expiration);
    }

    @Test
    @DisplayName("토큰 타입이 refresh가 아니면 예외 발생")
    void logout_invalidTokenType_throwsException() {
        // given
        doNothing().when(jwtProvider).validateOrThrow(refreshToken);
        when(jwtProvider.getTokenType(refreshToken)).thenReturn("access"); // 잘못된 타입

        // when & then
        assertThatThrownBy(() -> logoutService.logout(refreshToken))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.INVALID_TOKEN_TYPE.getMessage());
    }

    @Test
    @DisplayName("이미 블랙리스트에 등록된 토큰이면 예외 발생")
    void logout_alreadyBlacklisted_throwsException() {
        // given
        doNothing().when(jwtProvider).validateOrThrow(refreshToken);
        when(jwtProvider.getTokenType(refreshToken)).thenReturn("refresh");
        when(blacklistedTokenRepository.existsByToken(refreshToken)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> logoutService.logout(refreshToken))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.TOKEN_ALREADY_BLACKLISTED.getMessage());
    }
}
