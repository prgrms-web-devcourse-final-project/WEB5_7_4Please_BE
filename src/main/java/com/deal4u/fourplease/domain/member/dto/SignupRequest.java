package com.deal4u.fourplease.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "닉네임은 필수 입력값입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9_-]+$", message = "닉네임 형식이 잘못되었습니다.")
        String nickName
){}
