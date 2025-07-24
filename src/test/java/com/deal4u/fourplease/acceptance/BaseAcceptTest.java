package com.deal4u.fourplease.acceptance;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseAcceptTest {

    @LocalServerPort
    protected int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    protected RequestSpecification authRequest(Long memberId) {
        String assessToken = getAssessToke(getMember(memberId));

        return request().header("Authorization", "Bearer " + assessToken);
    }

    protected RequestSpecification request() {
        return RestAssured.given().port(port);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(AssertionError::new);
    }

    private String getAssessToke(Member member) {
        TokenPair tokenPair = jwtProvider.generateTokenPair(member);
        return tokenPair.accessToken();
    }
}
