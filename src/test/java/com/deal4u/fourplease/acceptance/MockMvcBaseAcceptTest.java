package com.deal4u.fourplease.acceptance;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public abstract class MockMvcBaseAcceptTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.webAppContextSetup(context);
    }


    protected MockMvcRequestSpecification authRequest(Long memberId) {
        String assessToken = getAssessToke(getMember(memberId));

        return request().header("Authorization", "Bearer " + assessToken);
    }

    protected MockMvcRequestSpecification request() {
        return RestAssuredMockMvc.given();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(AssertionError::new);
    }

    private String getAssessToke(Member member) {
        TokenPair tokenPair = jwtProvider.generateTokenPair(member);
        return tokenPair.accessToken();
    }
}
