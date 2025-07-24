package com.deal4u.fourplease.domain.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.filter.AuthMemberReader;
import com.deal4u.fourplease.domain.auth.filter.TokenExtraction;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseTokenTest {

    @Autowired
    private JwtProvider jwtProvider;

    @MockitoBean
    private AuthMemberReader  authMemberReader;

    @MockitoBean
    private TokenExtraction tokenExtraction;

    public void init(Member member){
        TokenPair tokenPair = jwtProvider.generateTokenPair(member);
        when(tokenExtraction.extract(any())).thenReturn(
                Optional.of(tokenPair.accessToken())
        );
        when(authMemberReader.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
    }
}
