package com.deal4u.fourplease.auth.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auth.model.Customoauth2User;
import com.deal4u.fourplease.domain.auth.service.CustomOauth2UserService;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.domain.member.service.MemberService;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
public class CustomOauth2UserServiceTest {
    private static final String TEST_EMAIL = "test@example.com";

    @InjectMocks
    private CustomOauth2UserService customOauth2UserService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate; // 주입된 delegate

    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        // OAuth2 사용자 속성 설정
        attributes = new HashMap<>();
        attributes.put("email", TEST_EMAIL);
        attributes.put("name", "Test User");
        attributes.put("picture", "https://example.com/picture.jpg");

        // OAuth2User Mock 설정
        when(oAuth2User.getAttributes()).thenReturn(attributes);
    }

    @Test
    @DisplayName("기존 회원이 존재할 때 해당 회원 정보로 OAuth2User 반환")
    void loadUser_ExistingMember_ReturnsCustomOauth2User() {
        // given
        Member existingMember = Member.builder()
                .email(TEST_EMAIL)
                .provider("google")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        when(delegate.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);
        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingMember));

        // when
        OAuth2User result = customOauth2UserService.loadUser(oAuth2UserRequest);

        // then
        assertTrue(result instanceof Customoauth2User);
        Customoauth2User customUser = (Customoauth2User) result;
        assertEquals(TEST_EMAIL, customUser.getMember().getEmail());
        assertEquals(attributes, customUser.getAttributes());

        verify(memberRepository).findByEmail(TEST_EMAIL);
        verify(memberService).validateMember(existingMember);
        verifyNoMoreInteractions(memberRepository, memberService);

    }


    @Test
    @DisplayName("기존 회원이 존재하지 않을 때 새 회원 생성 후 OAuth2User 반환")
    void loadUser_NewMember_CreatesNewMemberAndReturnsCustomOauth2User() {
        // Given
        when(delegate.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);
        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // 새로 저장될 Member 객체
        Member savedMember = Member.builder()
                .email(TEST_EMAIL)
                .provider("google")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // when
        OAuth2User result = customOauth2UserService.loadUser(oAuth2UserRequest);

        // then
        assertTrue(result instanceof Customoauth2User);
        Customoauth2User customUser = (Customoauth2User) result;
        assertEquals(TEST_EMAIL, customUser.getMember().getEmail());
        assertEquals(attributes, customUser.getAttributes());

        verify(memberRepository).findByEmail(TEST_EMAIL);
        verify(memberRepository).save(any(Member.class));
        verifyNoInteractions(memberService); // 기존 회원이 아니므로 validateMember 호출 X

    }

    @Test
    @DisplayName("OAuth2 속성에 이메일이 없을 때 예외 발생")
    void loadUser_NoEmail_ThrowsException() {
        // Given
        attributes.remove("email");
        when(delegate.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        // when & then
        assertThatThrownBy(() -> customOauth2UserService.loadUser(oAuth2UserRequest))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.OAUTH_EMAIL_NOT_FOUND.getMessage())
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST); // ← 실제 정의된 상태코드
    }

    @Test
    @DisplayName("OAuth2 속성에 이메일이 빈 문자열일 때 예외 발생")
    void loadUser_BlankEmail_ThrowsException() {
        // given
        attributes.put("email", ""); // 빈 문자열
        when(delegate.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        // when & then
        assertThatThrownBy(() -> customOauth2UserService.loadUser(oAuth2UserRequest))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.OAUTH_EMAIL_NOT_FOUND.getMessage())
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("OAuth2 속성에 이메일이 공백만 있을 때 예외 발생")
    void loadUser_WhitespaceEmail_ThrowsException() {
        // given
        attributes.put("email", "   "); // 공백 문자열
        when(delegate.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        // when & then
        assertThatThrownBy(() -> customOauth2UserService.loadUser(oAuth2UserRequest))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.OAUTH_EMAIL_NOT_FOUND.getMessage())
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

}

