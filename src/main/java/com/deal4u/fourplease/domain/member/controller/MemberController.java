package com.deal4u.fourplease.domain.member.controller;

import com.deal4u.fourplease.domain.member.dto.SignupRequest;
import com.deal4u.fourplease.domain.member.dto.SignupResponse;
import com.deal4u.fourplease.domain.member.dto.UpdateMemberRequest;
import com.deal4u.fourplease.domain.member.dto.UpdateMemberResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/signup/{token}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<SignupResponse> signUp(@PathVariable String token,
                                                 @RequestBody SignupRequest request) {
        return memberService.signup(token, request);
    }


    @PatchMapping("/members")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "422", description = "사용할 수 없는 닉네임")
    })
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UpdateMemberResponse> updateMember(
            @AuthenticationPrincipal Member member,
            @RequestBody UpdateMemberRequest request
    ) {
        return memberService.updateMember(member, request.nickName());
    }
}
