package com.deal4u.fourplease.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.repository.BlacklistedTokenRepository;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("Auth 관련 인수 테스트")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    private Member testMember;
    private TokenPair tokenPair;

    @BeforeEach
    void setUpTestData() {
        testMember = Member.builder()
                .email("test@example.com")
                .status(Status.ACTIVE)
                .provider("google")
                .role(com.deal4u.fourplease.domain.member.entity.Role.USER)
                .build();

        testMember = memberRepository.save(testMember);
        tokenPair = jwtProvider.generateTokenPair(testMember);
    }

    @Test
    @DisplayName("정상적인 refreshToken으로 accessToken 재발급에 성공한다")
    void refreshAccessToken_success() throws Exception {
        Cookie refreshTokenCookie = new Cookie("refreshToken", tokenPair.refreshToken());

        mockMvc.perform(post("/api/v1/auth/reissue/token")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization",
                        org.hamcrest.Matchers.startsWith("Bearer ")));
    }

    @Test
    @DisplayName("정상적인 요청으로 회원 탈퇴에 성공하고 204를 반환한다")
    void deactivateMember_success() throws Exception {
        Cookie refreshTokenCookie = new Cookie("refreshToken", tokenPair.refreshToken());

        mockMvc.perform(delete("/api/v1/auth/members")
                        .cookie(refreshTokenCookie)
                        .header("Authorization", "Bearer " + tokenPair.accessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        Member deleted = memberRepository.findById(testMember.getMemberId()).orElseThrow();
        assertThat(deleted.getStatus()).isEqualTo(Status.DELETED);
    }

    @Test
    @DisplayName("정상적인 refreshToken으로 로그아웃에 성공하고 204를 반환한다")
    void logout_success() throws Exception {
        Cookie refreshTokenCookie = new Cookie("refreshToken", tokenPair.refreshToken());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(refreshTokenCookie)
                        .header("Authorization", "Bearer " + tokenPair.accessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        boolean isBlacklisted = blacklistedTokenRepository.existsByToken(tokenPair.refreshToken());
        assertThat(isBlacklisted).isTrue();
    }
}
