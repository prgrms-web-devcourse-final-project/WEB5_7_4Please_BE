package com.deal4u.fourplease.domain.wishlist.controller;

import com.deal4u.fourplease.domain.auction.dto.WishlistCreateRequest;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.domain.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "위시리스트 관리 API")
public class WishlistController {

    private final WishlistService wishlistService;
    private final MemberRepository memberRepository;

    @Operation(summary = "위시리스트 추가")
    @ApiResponse(responseCode = "200", description = "위시리스트 추가 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createWishlist(
            @Valid @RequestBody WishlistCreateRequest request
    ) {
        // TODO: member 추후 수정 필요
        return wishlistService.save(request, memberRepository.findAll().getFirst());
    }

}
