package com.deal4u.fourplease.domain.auction.service;

import static com.deal4u.fourplease.domain.auction.validator.Validator.validateAuctionStatus;
import static com.deal4u.fourplease.domain.auction.validator.Validator.validateSeller;
import static com.deal4u.fourplease.global.exception.ErrorCode.AUCTION_NOT_FOUND;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionSearchRequest;
import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.dto.SellerInfoResponse;
import com.deal4u.fourplease.domain.auction.dto.SellerSaleListResponse;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.service.BidService;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.review.repository.ReviewRepository;
import com.deal4u.fourplease.domain.wishlist.repository.WishlistRepository;
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
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;

    private final BidService bidService;

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
    public AuctionDetailResponse getByAuctionId(Long auctionId, Member member) {
        BidSummaryDto bidSummaryDto = bidService.getBidSummaryDto(auctionId);

        Auction auction = auctionRepository.findByIdWithProduct(auctionId)
                .orElseThrow(AUCTION_NOT_FOUND::toException);

        List<String> productImageUrls = getProductImageUrls(auction.getProduct());

        boolean isWishList = isIsWishList(member, auction);

        return AuctionDetailResponse.toAuctionDetailResponse(
                auction,
                productImageUrls,
                bidSummaryDto,
                isWishList
        );
    }

    @Transactional
    public void deleteByAuctionId(Long auctionId, Member member) {
        Auction targetAuction = auctionRepository.findByIdWithProduct(auctionId)
                .orElseThrow(AUCTION_NOT_FOUND::toException);

        // Auction status 업데이트 후 낙찰된 경매는 취소 불가 기능 추가
        String status = targetAuction.getStatus().toString();
        validateAuctionStatus(status);

        // Seller가 member와 일치하지 않으면 403 예외 발생
        validateSeller(targetAuction.getProduct().getSeller(), member);

        // 경매 스케쥴 취소
        auctionScheduleService.cancelAuctionClose(targetAuction.getAuctionId());

        productService.deleteProduct(targetAuction.getProduct());
        targetAuction.delete();
    }

    @Transactional(readOnly = true)
    public PageResponse<AuctionListResponse> findAll(AuctionSearchRequest request, Member member) {
        Page<Auction> auctionPage = getAuctionPage(
                request.page(),
                request.size(),
                request.keyword(),
                request.categoryId(),
                request.order()
        );

        Page<AuctionListResponse> auctionListResponsePage =
                auctionSupportService.getAuctionListResponses(auctionPage, member);

        return PageResponse.fromPage(auctionListResponsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<SellerSaleListResponse> findSalesBySellerId(
            Long sellerId,
            Pageable pageable
    ) {
        Page<Auction> auctionPage = auctionRepository.findAllBySellerId(sellerId, pageable);

        Page<SellerSaleListResponse> sellerSaleListResponsePage =
                auctionSupportService.getSellerSaleListResponses(auctionPage);

        return PageResponse.fromPage(sellerSaleListResponsePage);
    }

    @Transactional(readOnly = true)
    public SellerInfoResponse getSellerInfo(Long auctionId) {
        Auction auction = getAuctionOrThrow(auctionId);

        Seller seller = auction.getProduct().getSeller();

        Long sellerId = seller.getMember().getMemberId();
        String sellerNickname = seller.getMember().getNickName();
        String createdAt = seller.getMember().getCreatedAt().toString();

        Integer totalReviews = reviewRepository.countBySellerMemberId(sellerId);
        Double averageRating = reviewRepository.getAverageRatingBySellerMemberId(sellerId);
        averageRating = averageRating != null ? averageRating : 0.0;

        Integer completedDeals = auctionRepository.countBySellerId(sellerId);

        return new SellerInfoResponse(
                sellerId,
                sellerNickname,
                totalReviews,
                Math.round(averageRating * 100.0) / 100.0,
                completedDeals,
                createdAt
        );
    }

    private Auction getAuctionOrThrow(Long auctionId) {
        return auctionRepository.findByIdWithProductAndSellerAndMember(auctionId)
                .orElseThrow(AUCTION_NOT_FOUND::toException);
    }

    private List<String> getProductImageUrls(Product product) {
        return productImageService.getByProduct(product)
                .toProductImageUrls();
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
        return isOrderByBidCount ? auctionRepository.findAllOrderByBidCount(pageable) :
                auctionRepository.findAll(pageable);
    }

    private Sort createSort(String order) {
        if (order.equals("timeout")) {
            return Sort.by(Sort.Direction.ASC, "duration.endTime");
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    private boolean isIsWishList(Member member, Auction auction) {
        boolean isWishList = false;

        if (member != null) {
            isWishList = wishlistRepository.findWishlist(auction, member.getMemberId()).isPresent();
        }
        return isWishList;
    }
}
