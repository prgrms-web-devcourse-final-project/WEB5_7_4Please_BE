package com.deal4u.fourplease.auth.handler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.handler.Oauth2AuthenticationSuccessHandler;
import com.deal4u.fourplease.domain.auth.model.Customoauth2User;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class Oauth2AuthenticationSuccessHandlerTest {
    @InjectMocks
    private Oauth2AuthenticationSuccessHandler successHandler;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private AuthService authService;

    @Mock
    private ObjectMapper objectMapper;

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
    private final String TEMP_TOKEN = "temp.jwt.token";
    private final String ACCESS_TOKEN = "access.jwt.token";
    private final String REFRESH_TOKEN = "refresh.jwt.token";

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


    @BeforeEach
    void setUp() throws IOException {
        when(authentication.getPrincipal()).thenReturn(customOauth2User);
        when(customOauth2User.getMember()).thenReturn(member);
        when(response.getWriter()).thenReturn(new PrintWriter(outputStream, true));
    }

//    @Test
//    @DisplayName("상태가 PENDING일 때 임시 토큰과 닉네임 설정 메시지 반환")
//    void onAuthenticationSuccess_PendingStatus_ReturnsTempTokenAndRedirectToSignup() throws Exception {
//        // given
//        when(member.getStatus()).thenReturn(Status.PENDING);
//        when(jwtProvider.generateTokenPair(member)).thenReturn(new TokenPair(TEMP_TOKEN, "ignored"));
//        when(member.getEmail()).thenReturn("test@example.com");
//
//        // when
//        successHandler.onAuthenticationSuccess(request, response, authentication);
//
//        // then
//        String responseJson = outputStream.toString();
//        assertThat(responseJson).contains("닉네임 설정이 필요합니다.");
//        assertThat(responseJson).contains(TEMP_TOKEN);
//        assertThat(responseJson).contains("/");
//    }

}
