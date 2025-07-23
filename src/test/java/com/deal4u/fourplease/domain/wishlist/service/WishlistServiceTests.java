package com.deal4u.fourplease.domain.wishlist.service;

import static com.deal4u.fourplease.domain.auction.util.TestUtils.genAuction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.dto.BidSummaryDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.service.AuctionService;
import com.deal4u.fourplease.domain.auction.service.AuctionSupportService;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistCreateRequest;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistResponse;
import com.deal4u.fourplease.domain.wishlist.entity.Wishlist;
import com.deal4u.fourplease.domain.wishlist.repository.WishlistRepository;
import com.deal4u.fourplease.global.exception.GlobalException;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class WishlistServiceTests {

    @InjectMocks
    private WishlistService wishlistService;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private AuctionService auctionService;

    @Mock
    private AuctionSupportService auctionSupportService;

    @Test
    @DisplayName("위시리스트를 등록할 수 있다")
    void saveShouldSaveWishlist() {

        Member member = mock(Member.class);
        Auction auction = mock(Auction.class);

        WishlistCreateRequest req = mock(WishlistCreateRequest.class);
        Wishlist wishlist = mock(Wishlist.class);

        when(req.auctionId()).thenReturn(1L);
        when(auctionService.getAuctionByAuctionId(1L)).thenReturn(auction);
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

        Long wishlistId = 1L;
        Wishlist wishlist = mock(Wishlist.class);

        when(wishlistRepository.findById(wishlistId)).thenReturn(Optional.of(wishlist));

        wishlistService.deleteByWishlistId(wishlistId);
        verify(wishlist).delete();

    }

    @Test
    @DisplayName("존재하지 않는 위시리스트 삭제를 시도하면 예외가 발생한다")
    void throwsIfWishlistIdNotExist() {

        when(wishlistRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> {
                    wishlistService.deleteByWishlistId(1L);
                }
        ).isInstanceOf(GlobalException.class).hasMessage("해당 위시리스트를 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("위시리스트를 조회할 수 있다")
    void findAllShouldReturnWishlistPageResponse() {

        Member member = mock(Member.class);
        Auction auction = genAuction();
        Wishlist wishlist = Wishlist.builder()
                .memberId(1L)
                .auction(auction)
                .deleted(false)
                .build();
        BidSummaryDto bidSummaryDto = mock(BidSummaryDto.class);

        Pageable pageable = PageRequest.of(0, 20);
        Page<Wishlist> wishlistPage = new PageImpl<>(List.of(wishlist));

        when(member.getMemberId()).thenReturn(1L);
        when(wishlistRepository.findAll(pageable, 1L)).thenReturn(wishlistPage);
        when(auctionSupportService.getBidSummaryDto(auction.getAuctionId()))
                .thenReturn(bidSummaryDto);

        PageResponse<WishlistResponse> resp = wishlistService.findAll(pageable, member);

        assertThat(resp.getContent()).hasSize(1);
        assertThat(resp.getContent().getFirst().name()).isEqualTo("칫솔");

    }

}