package com.deal4u.fourplease.domain.auth.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.entity.BlacklistedToken;
import com.deal4u.fourplease.domain.auth.repository.BlacklistedTokenRepository;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.time.LocalDateTime;
import java.util.Optional;
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
class AuthServiceTest {
    private final String refreshToken = "mocked.refresh.token";
    private final String email = "user@example.com";
    @InjectMocks
    private AuthService authService;
    @Mock
    private LogoutService logoutService;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;
    private Member member;
    private TokenPair tokenPair;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email(email)
                .status(Status.ACTIVE)
                .build();
        tokenPair = new TokenPair("access", "refresh");
    }

    @Test
    @DisplayName("기존 리프레시 토큰이 유효하면 새 토큰들을 반환")
    void refreshAccessToken_validToken_returnsNewTokenPair() {
        // given
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(7);
        doNothing().when(jwtProvider).validateOrThrow(refreshToken);
        when(jwtProvider.getTokenType(refreshToken)).thenReturn("refresh");
        when(blacklistedTokenRepository.existsByToken(refreshToken)).thenReturn(false);
        when(jwtProvider.getEmailFromToken(refreshToken)).thenReturn(email);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(jwtProvider.generateTokenPair(member)).thenReturn(tokenPair);
        when(jwtProvider.getExpirationFromToken(refreshToken)).thenReturn(expirationTime);


        // when
        TokenPair result = authService.refreshAccessToken(refreshToken);

        // then
        assertThat(result).isEqualTo(tokenPair);
        ArgumentCaptor<BlacklistedToken> captor = ArgumentCaptor.forClass(BlacklistedToken.class);
        verify(blacklistedTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getExpiryDate()).isEqualTo(expirationTime);
    }

    @Test
    @DisplayName("토큰 타입이 refresh가 아니면 예외 발생")
    void refreshAccessToken_invalidTokenType_throwsException() {
        // given
        when(jwtProvider.getTokenType(refreshToken)).thenReturn("access");

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.INVALID_TOKEN_TYPE.getMessage());
    }


    @Test
    @DisplayName("블랙리스트에 등록된 리프레시 토큰이면 예외 발생")
    void refreshAccessToken_blacklistedToken_throwsException() {
        // given
        when(jwtProvider.getTokenType(refreshToken)).thenReturn("refresh");
        when(blacklistedTokenRepository.existsByToken(refreshToken)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.TOKEN_ALREADY_BLACKLISTED.getMessage());
    }


    @Test
    @DisplayName("회원이 존재하지 않으면 예외 발생")
    void refreshAccessToken_memberNotFound_throwsException() {
        // given
        when(jwtProvider.getTokenType(refreshToken)).thenReturn("refresh");
        when(blacklistedTokenRepository.existsByToken(refreshToken)).thenReturn(false);
        when(jwtProvider.getEmailFromToken(refreshToken)).thenReturn(email);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("회원 탈퇴 시 로그아웃 처리 및 상태 변경")
    void deactivateMember_success() {
        // given
        when(logoutService.logout(refreshToken)).thenReturn(ResponseEntity.noContent().build());
        when(memberRepository.save(member)).thenReturn(member);

        // when
        ResponseEntity<Void> response = authService.deactivateMember(refreshToken, member);

        // then
        verify(logoutService).logout(refreshToken);
        verify(memberRepository).save(member);
        assertThat(member.getStatus()).isEqualTo(Status.DELETED);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

}
