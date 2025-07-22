package com.deal4u.fourplease.domain.auth.service;

import com.deal4u.fourplease.domain.auth.model.Customoauth2User;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        log.info("CustomOAuth2UserService.loadUser 진입");

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(request); // Spring이 가져온 사용자 정보

        Map<String, Object> attributes = oauth2User.getAttributes();

        String provider = request.getClientRegistration().getRegistrationId();
        String email = extractEmail(provider, attributes);

        if (email == null || email.isBlank()) {
            throw ErrorCode.OAUTH_EMAIL_NOT_FOUND.toException();
        }

        // 1. DB에 해당 이메일이 있는지 확인
        Optional<Member> optionalMember = memberRepository.findByEmailAndProvider(email, provider);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            // 탈퇴했던 회원이면 닉네임 재발급
            if (member.getStatus() == Status.DELETED) {
                member.setStatus(Status.PENDING);
            }
            return new Customoauth2User(member, attributes);
        }

        // 2. 없다면 생성 (최초 로그인)
        Member newMember = memberRepository.save(
                Member.builder()
                        .email(email)
                        .provider(provider)
                        .role(Role.USER)
                        .status(Status.PENDING)
                        .build());
        log.info("신규 회원 저장됨: " + newMember.getMemberId());
        return new Customoauth2User(newMember, attributes);
    }

    private String extractEmail(String provider, Map<String, Object> attributes) {
        if ("naver".equals(provider)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return (String) response.get("email");
        }
        // 기본은 구글
        return (String) attributes.get("email");
    }
}
