package com.deal4u.fourplease.domain.auction.controller;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.service.AuctionService;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
@Tag(name = "Auction", description = "상품-경매 관리 API")
public class AuctionController {

    private final AuctionService auctionService;
    private final MemberRepository memberRepository;

    @Operation(summary = "전체 경매 조회")
    @ApiResponse(responseCode = "200", description = "경매 목록 응답")
    @ApiResponse(responseCode = "404", description = "경매를 찾을 수 없음")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AuctionListResponse> readAllAuctions() {
        return auctionService.findAll();
    }

    @Operation(summary = "경매등록")
    @ApiResponse(responseCode = "201", description = "경매 등록 성공")
    @ApiResponse(responseCode = "400", description = "invalid value")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createAuction(
            @Valid @RequestBody AuctionCreateRequest request
            // TODO: member 추후 수정 필요
    ) {
        auctionService.save(request, memberRepository.findAll().getFirst());
    }

    @Operation(summary = "상품 설명")
    @ApiResponse(responseCode = "200", description = "상품 설명 반환")
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    @GetMapping("/{auctionId}/description")
    @ResponseStatus(HttpStatus.OK)
    public AuctionDetailResponse readAuction(
            @PathVariable(name = "auctionId") @Positive Long auctionId) {
        return auctionService.getByAuctionId(auctionId);
    }

    @Operation(summary = "경매제거")
    @ApiResponse(responseCode = "204", description = "경매 삭제 성공")
    @ApiResponse(responseCode = "409", description = "낙찰된 경매는 제거 불가능")
    @DeleteMapping("/{auctionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuction(@PathVariable(name = "auctionId") @Positive Long auctionId) {
        auctionService.deleteByAuctionId(auctionId);
    }

}
