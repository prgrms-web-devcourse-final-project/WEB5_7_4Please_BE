package com.deal4u.fourplease.domain.auction.service;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionSearchRequest;
import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.dto.SellerSaleListResponse;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.scheduler.AuctionScheduleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final ProductService productService;
    private final ProductImageService productImageService;
    private final AuctionScheduleService auctionScheduleService;

    private final AuctionSupportService auctionSupportService;

    @Transactional
    public void save(AuctionCreateRequest request, Member member) {
        ProductCreateDto productCreateDto = request.toProductCreateDto(member);
        Product product = productService.save(productCreateDto);

        Auction auction = request.toEntity(product);
        Auction save = auctionRepository.save(auction);

        // 경매 스케쥴 추가
        auctionScheduleService.scheduleAuctionClose(save.getAuctionId(),
                save.getDuration().getEndTime());
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

        // 경매 스케쥴 취소
        auctionScheduleService.cancelAuctionClose(targetAuction.getAuctionId());

        productService.deleteProduct(targetAuction.getProduct());
        targetAuction.delete();
    }

    @Transactional(readOnly = true)
    public PageResponse<AuctionListResponse> findAll(AuctionSearchRequest request) {
        Page<Auction> auctionPage = getAuctionPage(
                request.page(),
                request.size(),
                request.keyword(),
                request.categoryId(),
                request.order()
        );

        Page<AuctionListResponse> auctionListResponsePage =
                auctionSupportService.getAuctionListResponses(auctionPage);

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


    // TODO: auction 상태를 CLOSED로 변경하는 메서드로 대체 필요
    @Transactional
    public void close(Auction auction) {
        auction.close();
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

    private Page<Auction> getAuctionPage(
            int page,
            int size,
            String keyword,
            @Nullable Long categoryId,
            String order
    ) {
        boolean hasKeyword = !keyword.trim().isEmpty();
        boolean hasCategoryId = categoryId != null;
        boolean isOrderByBidCount = order.equals("bids");

        Pageable pageable = isOrderByBidCount ? PageRequest.of(page, size) :
                PageRequest.of(page, size, createSort(order));

        if (hasKeyword && hasCategoryId) {
            return isOrderByBidCount
                    ? auctionRepository.findByKeywordAndCategoryIdOrderByBidCount(
                    keyword,
                    categoryId,
                    pageable
            ) :
                    auctionRepository.findByKeywordAndCategoryId(keyword, categoryId, pageable);
        } else if (hasKeyword) {
            return isOrderByBidCount ? auctionRepository.findByKeywordOrderByBidCount(
                    keyword,
                    pageable
            ) :
                    auctionRepository.findByKeyword(keyword, pageable);
        } else if (hasCategoryId) {
            return isOrderByBidCount ? auctionRepository.findByCategoryIdOrderByBidCount(
                    categoryId,
                    pageable
            ) :
                    auctionRepository.findByCategoryId(categoryId, pageable);
        }
        return isOrderByBidCount ? auctionRepository.findAll(pageable) :
                auctionRepository.findAllOrderByBidCount(pageable);
    }

    private Sort createSort(String order) {
        if (order.equals("timeout")) {
            return Sort.by(Sort.Direction.ASC, "endTime");
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

}
