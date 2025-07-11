package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.global.exception.ErrorCode;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

    public AuctionDetailResponse getByAuctionId(@Positive Long auctionId) {
        List<Long> bidList = bidRepository.findPricesByAuctionIdOrderByPriceDesc(auctionId);

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(ErrorCode.AUCTION_NOT_FOUND::toException);

        Product product = auction.getProduct();

        List<String> productImageUrls = productImageService.getByProduct(product)
                .toProductImageUrlList();

        return new AuctionDetailResponse(
                BigDecimal.valueOf(bidList.getFirst()),
                auction.getInstantBidPrice(),
                bidList.size(),
                auction.getStartingPrice(),
                product.getName(),
                product.getCategory().getCategoryId(),
                product.getCategory().getName(),
                product.getDescription(),
                auction.getDuration().getEndTime(),
                product.getThumbnailUrl(),
                productImageUrls
        );
    }
}
