package com.deal4u.fourplease.global.scheduler;

import static com.deal4u.fourplease.testutil.TestUtils.genAuctionCreateRequest;
import static com.deal4u.fourplease.testutil.TestUtils.genMember;
import static com.deal4u.fourplease.testutil.TestUtils.genProduct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.auction.service.AuctionService;
import com.deal4u.fourplease.domain.auction.service.ProductService;
import com.deal4u.fourplease.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuctionScheduleServiceTest {

    @InjectMocks
    private AuctionService auctionService;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ProductService productService;

    @Mock
    private AuctionScheduleService auctionScheduleService;

    @Test
    @DisplayName("경매 생성 시 스케쥴이 정상적으로 등록")
    void save_should_register_schedule_on_create() {
        // Given
        Member member = genMember();
        AuctionCreateRequest req = genAuctionCreateRequest();
        Product product = genProduct();
        ProductCreateDto productDto = req.toProductCreateDto(member);

        // request.toEntity()는 ID가 없는 Auction 객체를 생성
        Auction auctionWithoutId = req.toEntity(product);
        Auction savedAuctionWithId = Auction.builder()
                .auctionId(1L)
                .product(product)
                .duration(auctionWithoutId.getDuration())
                .build();

        when(productService.save(productDto)).thenReturn(product);
        when(auctionRepository.save(any(Auction.class))).thenReturn(savedAuctionWithId);

        // when
        auctionService.save(req, member);

        // then - auction
        ArgumentCaptor<Auction> auctionCaptor = ArgumentCaptor.forClass(Auction.class);
        verify(auctionRepository).save(auctionCaptor.capture());
        Auction savedAuction = auctionCaptor.getValue();

        assertThat(savedAuction.getProduct()).isEqualTo(product);
        assertThat(savedAuction.getDuration().getEndTime()).isEqualTo(
                req.bidPeriod().getEndTime(req.startDate()));

        // then - schedule
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(auctionScheduleService, times(1)).scheduleAuctionClose(idCaptor.capture(),
                timeCaptor.capture());

        assertThat(idCaptor.getValue()).isEqualTo(savedAuctionWithId.getAuctionId());
        assertThat(timeCaptor.getValue()).isEqualTo(savedAuctionWithId.getDuration().getEndTime());
    }

    @Test
    @DisplayName("auctionId로 경매를 삭제하고 스케쥴을 취소")
    void delete_by_auction_id_should_delete_auction_and_cancel_schedule() {
        // Given
        Long auctionId = 1L;
        Auction auction = Auction.builder()
                .auctionId(auctionId)
                .product(genProduct())
                .build();

        when(auctionRepository.findByIdWithProduct(auctionId)).thenReturn(Optional.of(auction));

        // when
        auctionService.deleteByAuctionId(auctionId);

        // then - schedule
        verify(auctionScheduleService, times(1)).cancelAuctionClose(auctionId);

        // then
        verify(productService).deleteProduct(auction.getProduct());
        assertThat(auction.isDeleted()).isTrue();
    }
}