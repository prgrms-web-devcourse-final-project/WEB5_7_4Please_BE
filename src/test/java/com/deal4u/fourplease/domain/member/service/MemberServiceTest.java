package com.deal4u.fourplease.domain.member.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.service.AuthService;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.dto.SignupRequest;
import com.deal4u.fourplease.domain.member.dto.SignupResponse;
import com.deal4u.fourplease.domain.member.dto.UpdateMemberResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class MemberServiceTest {

    private final String validToken = "mocked.jwt.token";
    private final String email = "test@example.com";
    private final String nickname = "테스터";
    @InjectMocks
    private MemberService memberService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private AuthService authService;


    @Test
    @DisplayName("회원가입 성공 시 토큰 재발급 및 닉네임 설정")
    void signup_success() {
        SignupRequest request = new SignupRequest(nickname);
        Member member = Member.builder()
                .email(email)
                .status(Status.PENDING)
                .build();
        when(jwtProvider.getEmailFromToken(validToken)).thenReturn(email);
        doNothing().when(jwtProvider).validateOrThrow(validToken);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(memberRepository.existsByNickName(nickname)).thenReturn(false);
        when(authService.createTokenPair(member)).thenReturn(new TokenPair("access", "refresh"));

        SignupResponse response = memberService.signup(validToken, request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.message()).contains("로그인 성공");
        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        assertThat(response.redirectUrl()).isEqualTo("/");

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getNickName()).isEqualTo(nickname);
        assertThat(captor.getValue().getStatus()).isEqualTo(Status.ACTIVE);
    }

    // 나중에 닉네임 체크 개선되면 예외 추가
    @Test
    @DisplayName("회원가입 중 회원이 존재하지 않으면 예외 발생")
    void signup_shouldFailIfMemberNotFound() {
        SignupRequest request = new SignupRequest(nickname);
        when(jwtProvider.getEmailFromToken(validToken)).thenReturn(email);
        doNothing().when(jwtProvider).validateOrThrow(validToken);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.signup(validToken, request))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("회원 정보 업데이트 성공 시 닉네임 변경 및 메시지 반환")
    void updateMember_success() {
        String newNickName = "NewNickName";
        Member member = Member.builder()
                .email(email)
                .status(Status.ACTIVE)
                .build();

        when(memberRepository.existsByNickName(newNickName)).thenReturn(false);
        when(memberRepository.save(member)).thenReturn(member);

        UpdateMemberResponse response = memberService.updateMember(member, newNickName).getBody();

        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("업데이트 성공");
        assertThat(response.nickName()).isEqualTo(newNickName);
        assertThat(member.getNickName()).isEqualTo(newNickName);
    }

    @Test
    @DisplayName("정상 닉네임은 예외 없이 통과")
    void validateNickname_valid() {
        String nickName = "정상닉네임123";
        when(memberRepository.existsByNickName(nickName)).thenReturn(false);

        memberService.validateNickName(nickName); // 예외 발생 안 하면 통과
    }

    @Test
    @DisplayName("null 닉네임은 예외 발생")
    void validateNickname_null() {
        assertThatThrownBy(() -> memberService.validateNickName(null))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.INVALID_NICKNAME.getMessage());
    }

    @Test
    @DisplayName("공백 닉네임은 예외 발생")
    void validateNickname_blank() {
        assertThatThrownBy(() -> memberService.validateNickName("   "))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.INVALID_NICKNAME.getMessage());
    }

    @Test
    @DisplayName("2자 미만 또는 20자 초과 닉네임은 예외 발생")
    void validateNickname_lengthOutOfBounds() {
        assertThatThrownBy(() -> memberService.validateNickName("a"))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.NICKNAME_LENGTH_INVALID.getMessage());

        assertThatThrownBy(() -> memberService.validateNickName("a".repeat(21)))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.NICKNAME_LENGTH_INVALID.getMessage());
    }

    @Test
    @DisplayName("허용되지 않은 특수문자가 포함되면 예외 발생")
    void validateNickname_invalidCharacters() {
        assertThatThrownBy(() -> memberService.validateNickName("닉네임!@#"))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.INVALID_NICKNAME_FORMAT.getMessage());
    }

    @Test
    @DisplayName("중복된 닉네임이면 예외 발생")
    void validateNickname_duplicate() {
        String nickName = "중복닉";
        when(memberRepository.existsByNickName(nickName)).thenReturn(true);

        assertThatThrownBy(() -> memberService.validateNickName(nickName))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.NICKNAME_ALREADY_EXISTS.getMessage());
    }
}
