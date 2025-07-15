package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final ProductService productService;
    private final BidRepository bidRepository;
    private final ProductImageService productImageService;

    @Transactional
    public void save(AuctionCreateRequest request, Member member) {
        ProductCreateDto productCreateDto = request.toProductCreateDto(member);
        Product product = productService.save(productCreateDto);

        Auction auction = request.toEntity(product);
        auctionRepository.save(auction);
    }

    @Transactional(readOnly = true)
    public AuctionDetailResponse getByAuctionId(Long auctionId) {
        BidSummaryDto bidSummaryDto = getBidSummaryDto(auctionId);

        Auction auction = auctionRepository.findByIdWithProduct(auctionId)
                .orElseThrow(ErrorCode.AUCTION_NOT_FOUND::toException);

        List<String> productImageUrlList = getProductImageUrlList(auction.getProduct());

        return AuctionDetailResponse.toAuctionDetailResponse(
                auction,
                productImageUrlList,
                bidSummaryDto
        );
    }

    @Transactional
    public void deleteByAuctionId(Long auctionId) {
        Auction targetAuction = auctionRepository.findByIdWithProduct(auctionId)
                .orElseThrow(ErrorCode.AUCTION_NOT_FOUND::toException);

        productService.deleteProduct(targetAuction.getProduct());
        targetAuction.delete();
    }

    @Transactional(readOnly = true)
    public List<AuctionListResponse> findAll() {
        List<AuctionListResponse> auctionListResponseList = new ArrayList<>();

        List<Auction> auctionList = auctionRepository.findAll();

        for (Auction auction : auctionList) {
            BidSummaryDto bidSummaryDto = getBidSummaryDto(auction.getAuctionId());

            auctionListResponseList.add(
                    AuctionListResponse.toAuctionListResponse(
                            auction,
                            bidSummaryDto,
                            false
                    )
            );
        }

        return auctionListResponseList;
    }

    private BidSummaryDto getBidSummaryDto(Long auctionId) {
        List<BigDecimal> bidList = bidRepository.findPricesByAuctionIdOrderByPriceDesc(
                auctionId
        );
        return BidSummaryDto.toBidSummaryDto(bidList);
    }

    private List<String> getProductImageUrlList(Product product) {
        return productImageService.getByProduct(product)
                .toProductImageUrlList();
    }
}
