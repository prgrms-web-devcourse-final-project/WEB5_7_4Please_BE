package com.deal4u.fourplease.domain.auth.service;

import com.deal4u.fourplease.domain.auth.model.CustomOAuth2User;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        log.info("CustomOAuth2UserService.loadUser 진입");
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        //소셜로부터 유저 정보 받아옴
        OAuth2User oAuth2User = delegate.loadUser(request); // Spring이 가져온 사용자 정보
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");

        // 1. DB에 해당 이메일이 있는지 확인
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        log.info("받아온 email: " + email);
        log.info("기존 회원 여부: " + optionalMember.isPresent());
        if (optionalMember.isPresent()) {
            return new CustomOAuth2User(optionalMember.get(), attributes);
        }

        // 2. 없다면 생성
        Member newMember = memberRepository.save(
                Member.builder()
                .email(email)
                .provider("google")
                .role(Role.USER)
                .status(Status.PENDING)
                .build());
        log.info("신규 회원 저장됨: " + newMember.getMemberId());
        return new CustomOAuth2User(newMember, attributes);

    }
}
