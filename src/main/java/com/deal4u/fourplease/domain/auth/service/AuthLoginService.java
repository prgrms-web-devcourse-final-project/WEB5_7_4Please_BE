package com.deal4u.fourplease.domain.auth.service;

import com.deal4u.fourplease.domain.auth.client.GoogleOAuthClient;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthLoginService {
    private GoogleOAuthClient googleOAuthClient;


    public ResponseEntity<?> login(String type, String code) {
        if (!type.equalsIgnoreCase("google")) {
            throw new IllegalArgumentException("지원하지 않는 Oauth 입니다!");
        }

        // SNS에서 받은 code로 SNS의 access token 발급
        String accessToken = googleOAuthClient.getAccessToken(code);

        // 사용자 정보 조회
        return ResponseEntity.ok(accessToken);
    }
}
