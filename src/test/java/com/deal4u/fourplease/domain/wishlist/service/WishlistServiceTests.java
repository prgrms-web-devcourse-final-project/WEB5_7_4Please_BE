package com.deal4u.fourplease.domain.wishlist.service;

import static com.deal4u.fourplease.testutil.TestUtils.genAuction;
import static com.deal4u.fourplease.testutil.TestUtils.genMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.service.AuctionReaderImpl;
import com.deal4u.fourplease.domain.bid.service.BidService;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistCreateRequest;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistResponse;
import com.deal4u.fourplease.domain.wishlist.entity.Wishlist;
import com.deal4u.fourplease.domain.wishlist.repository.WishlistRepository;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Slf4j
@ExtendWith(MockitoExtension.class)
class WishlistServiceTests {

    @InjectMocks
    private WishlistService wishlistService;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private AuctionReaderImpl auctionReaderImpl;

    @Mock
    private BidService bidService;

    @Test
    @DisplayName("위시리스트를 등록할 수 있다")
    void saveShouldSaveWishlist() {

        Member member = mock(Member.class);
        Auction auction = mock(Auction.class);

        WishlistCreateRequest req = mock(WishlistCreateRequest.class);
        Wishlist wishlist = mock(Wishlist.class);

        when(req.auctionId()).thenReturn(1L);
        when(auctionReaderImpl.getAuctionByAuctionId(1L)).thenReturn(auction);
        when(req.toEntity(member, auction)).thenReturn(wishlist);
        when(wishlist.getWishlistId()).thenReturn(1L);
        when(wishlistRepository.save(wishlist)).thenReturn(wishlist);

        Long resp = wishlistService.save(req, member);

        assertThat(resp).isEqualTo(1L);
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    @DisplayName("위시리스트를 삭제할 수 있다")
    void deleteShouldDeleteWishlistByWishlistId() {

        Long auctionId = 1L;
        Member member = Member.builder()
                .memberId(1L)
                .build();
        Auction auction = genAuction();

        Wishlist wishlist = Wishlist.builder().auction(auction).memberId(member.getMemberId())
                .deleted(false).build();

        when(auctionReaderImpl.getAuctionByAuctionId(auctionId)).thenReturn(auction);

        when(wishlistRepository.findWishlist(eq(auction), eq(member.getMemberId())))
                .thenReturn(Optional.of(wishlist));

        wishlistService.deleteByWishlistId(auctionId, member);

        assertThat(wishlist.isDeleted()).isTrue();

    }

    @Test
    @DisplayName("존재하지 않는 위시리스트 삭제를 시도하면 예외가 발생한다")
    void throwsIfWishlistIdNotExist() {

        Long auctionId = 1L;
        Member member = genMember();
        Auction auction = genAuction();

        when(auctionReaderImpl.getAuctionByAuctionId(auctionId)).thenReturn(auction);

        when(wishlistRepository.findWishlist(eq(auction), eq(member.getMemberId())))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> {
                    wishlistService.deleteByWishlistId(auctionId, genMember());
                }
        ).isInstanceOf(GlobalException.class).hasMessage("해당 위시리스트를 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("일치하지 않는 사용자가 위시리스트 삭제를 시도하면 예외가 발생한다")
    void throwsIfTryToDeleteWishlistByDifferentMember() {

        Long auctionId = 1L;
        Member currentMember = Member.builder().memberId(1L).build();
        Member wishlistOwner = Member.builder().memberId(99L).build();
        Auction auction = genAuction();

        Wishlist wishlist = Wishlist.builder()
                .auction(auction)
                .memberId(wishlistOwner.getMemberId())
                .deleted(false)
                .build();

        when(auctionReaderImpl.getAuctionByAuctionId(auctionId)).thenReturn(auction);

        when(wishlistRepository.findWishlist(eq(auction), eq(currentMember.getMemberId())))
                .thenReturn(Optional.of(wishlist));

        assertThatThrownBy(
                () -> {
                    wishlistService.deleteByWishlistId(auctionId, currentMember);
                }
        ).isInstanceOf(GlobalException.class).hasMessage("권한이 없습니다.");

    }

    @Test
    @DisplayName("위시리스트를 조회할 수 있다")
    void findAllShouldReturnWishlistPageResponse() {

        Member member = mock(Member.class);
        Pageable pageable = PageRequest.of(0, 20);

        Auction auction1 = Auction.builder()
                .auctionId(1L)
                .product(Product.builder()
                        .name("목도리")
                        .thumbnailUrl("http://example.com/thumbnail1.jpg")
                        .build()
                )
                .startingPrice(BigDecimal.valueOf(1000000))
                .build();

        Auction auction2 = Auction.builder()
                .auctionId(2L)
                .product(Product.builder()
                        .name("축구공")
                        .thumbnailUrl("http://example.com/thumbnail2.jpg")
                        .build()
                )
                .startingPrice(BigDecimal.valueOf(5000000))
                .build();

        Wishlist wishlist1 = Wishlist.builder()
                .auction(auction1)
                .memberId(1L)
                .deleted(false)
                .build();

        Wishlist wishlist2 = Wishlist.builder()
                .auction(auction2)
                .memberId(1L)
                .deleted(false)
                .build();

        List<Wishlist> wishlistList = List.of(wishlist1, wishlist2);
        Page<Wishlist> wishlistPage = new PageImpl<>(wishlistList, pageable, wishlistList.size());

        Map<Long, BidSummaryDto> bidSummaryDtoMap = Map.of(
                1L, new BidSummaryDto(BigDecimal.valueOf(2000000), 5),
                2L, new BidSummaryDto(BigDecimal.valueOf(10000000), 20)
        );

        when(member.getMemberId()).thenReturn(1L);
        when(wishlistRepository.findAll(pageable, 1L)).thenReturn(wishlistPage);
        when(bidService.getBidSummaryDtoMap(List.of(1L, 2L))).thenReturn(bidSummaryDtoMap);

        PageResponse<WishlistResponse> response = wishlistService.findAll(pageable, member);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);

        WishlistResponse first = response.getContent().get(0);
        assertThat(first.name()).isEqualTo("목도리");
        assertThat(first.thumbnailUrl()).isEqualTo("http://example.com/thumbnail1.jpg");
        assertThat(first.maxPrice()).isEqualTo(BigDecimal.valueOf(2000000));
        assertThat(first.bidCount()).isEqualTo(5);

        WishlistResponse second = response.getContent().get(1);
        assertThat(second.name()).isEqualTo("축구공");
        assertThat(second.thumbnailUrl()).isEqualTo("http://example.com/thumbnail2.jpg");
        assertThat(second.maxPrice()).isEqualTo(BigDecimal.valueOf(10000000));
        assertThat(second.bidCount()).isEqualTo(20);

        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(20);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(1);
    }

}