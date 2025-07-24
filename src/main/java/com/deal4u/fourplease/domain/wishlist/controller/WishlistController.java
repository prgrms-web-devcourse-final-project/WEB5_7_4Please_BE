package com.deal4u.fourplease.domain.wishlist.controller;

import static com.deal4u.fourplease.domain.wishlist.mapper.WishlistMapper.getSort;

import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistCreateRequest;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistResponse;
import com.deal4u.fourplease.domain.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "위시리스트 관리 API")
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "위시리스트 추가")
    @ApiResponse(responseCode = "200", description = "위시리스트 추가 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createWishlist(
            @Valid @RequestBody WishlistCreateRequest request,
            @AuthenticationPrincipal Member member
    ) {
        return wishlistService.save(request, member);
    }


    @Operation(summary = "위시리스트 목록 조회")
    @ApiResponse(responseCode = "200", description = "위시리스트 목록 조회 성공")
    @Parameter(description = "정렬 옵션: latest, earliest")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<WishlistResponse> readAllWishlist(
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(0) @Max(100) int size,
            @RequestParam(name = "order", defaultValue = "latest") String order
            @AuthenticationPrincipal Member member
    ) {
        Pageable pageable = PageRequest.of(page, size, getSort(order));

        return wishlistService.findAll(pageable, member);
    }

    @Operation(summary = "위시리스트 삭제")
    @ApiResponse(responseCode = "204", description = "위시리스트 삭제 성공")
    @ApiResponse(responseCode = "404", description = "해당 위시리스트를 찾을 수 없음")
    @DeleteMapping("/{wishlistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWishlist(
            @PathVariable(name = "wishlistId") @Positive Long wishlistId
    ) {
        wishlistService.deleteByWishlistId(wishlistId);
    }

}
