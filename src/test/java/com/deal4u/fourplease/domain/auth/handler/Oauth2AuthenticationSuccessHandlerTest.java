package com.deal4u.fourplease.domain.auth.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.model.Customoauth2User;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class Oauth2AuthenticationSuccessHandlerTest {
    @InjectMocks
    private Oauth2AuthenticationSuccessHandler successHandler;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private AuthService authService;
    @Mock
    private MemberService memberService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Authentication authentication;
    @Mock
    private Customoauth2User customOauth2User;
    @Mock
    private Member member;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();

        when(authentication.getPrincipal()).thenReturn(customOauth2User);
        when(customOauth2User.getMember()).thenReturn(member);
    }

    @Test
    @DisplayName("상태가 PENDING일 때 임시 토큰과 리디렉션 헤더 반환")
    void onAuthenticationSuccessPendingStatusReturnsTempTokenAndRedirectToSignup()
            throws Exception {
        // given
        when(response.getWriter()).thenReturn(new PrintWriter(outputStream, true));
        String tempToken = "temp.jwt.token";
        when(member.getStatus()).thenReturn(Status.PENDING);
        when(jwtProvider.generateTokenPair(member)).thenReturn(
                new TokenPair(tempToken, "ignored"));
        when(member.getEmail()).thenReturn("test@example.com");

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String expectedRedirectJson = String.format("{\"token\":\"%s\"}", tempToken);
        assertThat(outputStream.toString()).contains(expectedRedirectJson);
    }

    @Test
    @DisplayName("상태가 ACTIVE일 때 accessToken 헤더와 refreshToke 쿠키 반환")
    void onAuthenticationSuccessActiveStatusReturnsTokenPairAndRedirectToMain() throws Exception {
        // given
        String accessToken = "access.jwt.token";
        String refreshToken = "refresh.jwt.token";
        String nickname = "홍길동";
        when(member.getStatus()).thenReturn(Status.ACTIVE);
        when(authService.createTokenPair(member)).thenReturn(
                new TokenPair(accessToken, refreshToken));
        when(member.getEmail()).thenReturn("test@example.com");
        when(member.getNickName()).thenReturn(nickname);
        when(response.getWriter()).thenReturn(new PrintWriter(outputStream, true));


        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(response).setHeader("Authorization", "Bearer " + accessToken);
        verify(response).addHeader(eq("Set-Cookie"), contains("refreshToken=")); // 쿠키 확인
        verify(response).setStatus(HttpServletResponse.SC_OK);
        String expectedJson = String.format("{\"nickname\":\"%s\"}", nickname);
        assertThat(outputStream.toString()).contains(expectedJson);
    }
}
