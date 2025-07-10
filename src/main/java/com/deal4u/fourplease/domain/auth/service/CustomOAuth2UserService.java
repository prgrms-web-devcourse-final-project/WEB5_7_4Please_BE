package com.deal4u.fourplease.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private AuthService authService;

//    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest){
//        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
//
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//        String name = (String) attributes.get("name");
//        String email = (String) attributes.get("email");
//        return ;
//    }

}
