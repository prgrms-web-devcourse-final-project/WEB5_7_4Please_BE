package com.deal4u.fourplease.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.dto.SignupRequest;
import com.deal4u.fourplease.domain.member.dto.SignupResponse;
import com.deal4u.fourplease.domain.member.dto.UpdateMemberRequest;
import com.deal4u.fourplease.domain.member.dto.UpdateMemberResponse;
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

@DisplayName("회원가입 관련 인수 테스트")
@Transactional
class MemberTest extends MockMvcBaseAcceptTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private Member testMember;
    private String token;

    @BeforeEach
    void setUpTestData() {
        testMember = Member.builder()
                .email("test@example.com")
                .status(Status.PENDING)
                .provider("google")
                .role(com.deal4u.fourplease.domain.member.entity.Role.USER)
                .build();
        testMember = memberRepository.save(testMember);
        token = jwtProvider.generateTokenPair(testMember).accessToken();
    }

    @Test
    @DisplayName("닉네임을 설정하면 회원가입이 완료되고 토큰이 재발급된다")
    void signupSuccess() {
        SignupRequest requestDto = new SignupRequest("새유저닉네임");

        SignupResponse response = authRequest(testMember.getMemberId())
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/v1/signup/{token}", token)
                .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(SignupResponse.class);

        assertThat(response.message()).isEqualTo("닉네임 설정 완료, 로그인 성공");
        assertThat(response.redirectUrl()).isEqualTo("/");
        assertThat(response.accessToken()).isNotBlank();

        Member updated = memberRepository.findById(testMember.getMemberId()).orElseThrow();
        assertThat(updated.getNickName()).isEqualTo("새유저닉네임");
        assertThat(updated.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("닉네임이 비어있으면 400 에러가 발생한다")
    void signupFailInvalidNickname() {
        SignupRequest requestDto = new SignupRequest(" ");

        authRequest(testMember.getMemberId())
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/v1/signup/{token}", token)
                .then()
                .log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("중복 닉네임이면 422 에러가 발생한다")
    void signupFailDuplicateNickname() {
        memberRepository.save(Member.builder()
                .email("other@example.com")
                .nickName("중복닉네임")
                .status(Status.ACTIVE)
                .provider("google")
                .role(com.deal4u.fourplease.domain.member.entity.Role.USER)
                .build());

        SignupRequest requestDto = new SignupRequest("중복닉네임");

        authRequest(testMember.getMemberId())
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/v1/signup/{token}", token)
                .then()
                .log().all()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    @DisplayName("닉네임을 정상적으로 변경할 수 있다")
    void updateSuccess() {
        UpdateMemberRequest request = new UpdateMemberRequest("변경된닉네임");

        UpdateMemberResponse response = authRequest(testMember.getMemberId())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/v1/members")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(UpdateMemberResponse.class);

        assertThat(response.nickName()).isEqualTo("변경된닉네임");
        assertThat(response.message()).isEqualTo("업데이트 성공");
    }
}
