package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.dto.PageResponse;
import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.dto.SellerSaleListResponse;
import com.deal4u.fourplease.domain.auction.dto.WishlistCreateRequest;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final ProductService productService;
    private final ProductImageService productImageService;

    private final AuctionSupportService auctionSupportService;

    @Transactional
    public void save(AuctionCreateRequest request, Member member) {
        ProductCreateDto productCreateDto = request.toProductCreateDto(member);
        Product product = productService.save(productCreateDto);

        Auction auction = request.toEntity(product);
        auctionRepository.save(auction);
    }

    @Transactional(readOnly = true)
    public AuctionDetailResponse getByAuctionId(Long auctionId) {
        BidSummaryDto bidSummaryDto = auctionSupportService.getBidSummaryDto(auctionId);

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
    public PageResponse<AuctionListResponse> findAll(Pageable pageable) {
        Page<Auction> auctionPage = auctionRepository.findAll(pageable);

        Page<AuctionListResponse> auctionListResponsePage = auctionSupportService
                .getAuctionListResponses(auctionPage);

        return PageResponse.fromPage(auctionListResponsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<SellerSaleListResponse> findSalesBySellerId(
            Long sellerId,
            Pageable pageable
    ) {
        List<Product> productList = productService.getProductListBySellerId(sellerId);

        List<Long> productIdList = productList.stream()
                .map(Product::getProductId)
                .toList();

        Page<Auction> auctionPage = auctionRepository.findAllByProductIdIn(productIdList, pageable);

        Page<SellerSaleListResponse> sellerSaleListResponsePage = auctionPage
                .map(auction -> {
                    BidSummaryDto bidSummaryDto = auctionSupportService
                            .getBidSummaryDto(auction.getAuctionId());
                    return SellerSaleListResponse.toSellerSaleListResponse(
                            auction,
                            bidSummaryDto,
                            auctionSupportService.getSaleAuctionStatus(auction)
                    );
                });

        return PageResponse.fromPage(sellerSaleListResponsePage);
    }

    @Transactional(readOnly = true)
    public Auction getAuctionByAuctionId(Long auctionId) {
        return auctionRepository.findByIdWithProduct(auctionId)
                .orElseThrow(ErrorCode.AUCTION_NOT_FOUND::toException);
    }

    private List<String> getProductImageUrlList(Product product) {
        return productImageService.getByProduct(product)
                .toProductImageUrlList();
    }

}
