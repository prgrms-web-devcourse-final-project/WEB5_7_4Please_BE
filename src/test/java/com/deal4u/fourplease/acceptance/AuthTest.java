package com.deal4u.fourplease.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("Auth 관련 인수 테스트")
@Transactional
class AuthAcceptanceTest extends MockMvcBaseAcceptTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private Member testMember;
    private TokenPair tokenPair;

    @BeforeEach
    void setUpTestData() {
        testMember = Member.builder()
                .email("test@example.com")
                .status(Status.ACTIVE)
                .provider("kakao")
                .role(com.deal4u.fourplease.domain.member.entity.Role.USER)
                .build();

        testMember = memberRepository.save(testMember);
        tokenPair = jwtProvider.generateTokenPair(testMember);
    }

    @Test
    @DisplayName("정상적인 refreshToken으로 accessToken 재발급에 성공한다")
    void refreshAccessToken_success() {
        request()
                .cookie("refreshToken", tokenPair.refreshToken())
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/reissue/token")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .header("Authorization", org.hamcrest.Matchers.startsWith("Bearer "));
    }

    @Test
    @DisplayName("정상적인 요청으로 회원 탈퇴에 성공하고 204를 반환한다")
    void deactivateMember_success() {
        authRequest(testMember.getMemberId())
                .cookie("refreshToken", tokenPair.refreshToken())
                .when()
                .delete("/api/v1/members")
                .then()
                .log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        Member deleted = memberRepository.findById(testMember.getMemberId()).orElseThrow();
        assertThat(deleted.getStatus()).isEqualTo(Status.DELETED);
    }
}
