package com.deal4u.fourplease.domain.member.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
class MyPageBidHistoryServiceTest {

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private MyPageBidHistoryService myPageBidHistoryService;

    @Test
    @DisplayName("내 입찰 내역을 페이지네이션으로 조회한다")
    void getMyBidHistorySuccess() {
        // given
        Long memberId = 2L;
        Pageable pageable = PageRequest.of(0, 10);

        List<MyPageBidHistory> mockBidHistories = createMockBidHistories();
        Page<MyPageBidHistory> mockPage =
                new PageImpl<>(mockBidHistories, pageable, mockBidHistories.size());

        given(bidRepository.findMyBidHistoryH2(eq(memberId), any(Pageable.class)))
                .willReturn(mockPage);

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(9);
        assertThat(result.getTotalElements()).isEqualTo(9);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getPage()).isZero();
        assertThat(result.getSize()).isEqualTo(10);

        verify(bidRepository).findMyBidHistoryH2(memberId, pageable);
    }

    @Test
    @DisplayName("빈 결과에 대해서도 정상적으로 PageResponse를 반환한다")
    void getMyBidHistoryEmptyResult() {
        // given
        Long memberId = 2L;
        Pageable pageable = PageRequest.of(0, 10);

        Page<MyPageBidHistory> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        given(bidRepository.findMyBidHistoryH2(eq(memberId), any(Pageable.class)))
                .willReturn(emptyPage);

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();

        verify(bidRepository).findMyBidHistoryH2(memberId, pageable);
    }

    @Test
    @DisplayName("다양한 상태의 입찰 내역을 올바르게 조회한다")
    void getMyBidHistoryVariousStatuses() {
        // given
        Long memberId = 2L;
        Pageable pageable = PageRequest.of(0, 10);

        List<MyPageBidHistory> mockBidHistories = createMockBidHistoriesWithVariousStatuses();
        Page<MyPageBidHistory> mockPage =
                new PageImpl<>(mockBidHistories, pageable, mockBidHistories.size());

        given(bidRepository.findMyBidHistoryH2(eq(memberId), any(Pageable.class)))
                .willReturn(mockPage);

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(pageable);

        // then
        assertThat(result.getContent()).hasSize(4);

        // 상태별로 확인
        List<MyPageBidHistory> content = result.getContent();
        assertThat(content.get(0).statusDescription()).isEqualTo("구매확정"); // SUCCESS + DELIVERED
        assertThat(content.get(1).statusDescription()).isEqualTo("낙찰"); // PENDING
        assertThat(content.get(2).statusDescription()).isEqualTo("결제 실패"); // REJECTED
        assertThat(content.get(3).statusDescription()).isEqualTo("패찰"); // FAIL

        verify(bidRepository).findMyBidHistoryH2(memberId, pageable);
    }

    @Test
    @DisplayName("페이지네이션이 올바르게 동작한다")
    void getMyBidHistoryPagination() {
        // given
        Long memberId = 2L;
        Pageable pageable = PageRequest.of(1, 5); // 두 번째 페이지, 5개씩

        List<MyPageBidHistory> mockBidHistories = createMockBidHistories().subList(0, 5);
        Page<MyPageBidHistory> mockPage = new PageImpl<>(mockBidHistories, pageable, 10);

        given(bidRepository.findMyBidHistoryH2(eq(memberId), any(Pageable.class)))
                .willReturn(mockPage);

        // when
        PageResponse<MyPageBidHistory> result = myPageBidHistoryService.getMyBidHistory(pageable);

        // then
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);

        verify(bidRepository).findMyBidHistoryH2(memberId, pageable);
    }

    private List<MyPageBidHistory> createMockBidHistories() {
        LocalDateTime now = LocalDateTime.now();

        return Arrays.asList(
                // 진행중인 경매 (auction_id: 1)
                new MyPageBidHistory(1L, 1L, "https://example.com/images/laptop.jpg", "최신형 노트북",
                        "진행중", BigDecimal.valueOf(100000.0), BigDecimal.valueOf(200000.0),
                        BigDecimal.valueOf(800000.0), BigDecimal.valueOf(200000.0), "입찰자A",
                        BigDecimal.valueOf(0.0),
                        now.minusSeconds(20), now.minusSeconds(20), "", "판매자"),

                // 낙찰된 경매 (auction_id: 2) - 구매확정
                new MyPageBidHistory(2L, 2L, "https://example.com/images/iPhone16.jpg",
                        "최신형 IPhone 16",
                        "구매확정", BigDecimal.valueOf(1200000.0), BigDecimal.valueOf(199000.0),
                        BigDecimal.valueOf(1500000.0), BigDecimal.valueOf(199000.0), "입찰자A",
                        BigDecimal.valueOf(199000.0),
                        now.minusSeconds(19), now.minusSeconds(19), now.plusDays(7).toString(),
                        "판매자"),

                // 패찰된 경매 (auction_id: 3)
                new MyPageBidHistory(3L, 3L, "https://example.com/images/iPhone16.jpg",
                        "최신형 IPhone 17",
                        "패찰", BigDecimal.valueOf(1200000.0), BigDecimal.valueOf(198000.0),
                        BigDecimal.valueOf(1500000.0), BigDecimal.valueOf(198000.0), "",
                        BigDecimal.valueOf(0.0),
                        now.minusSeconds(18), now.minusSeconds(18), "", "판매자"),

                // 결제 완료 (auction_id: 4)
                new MyPageBidHistory(4L, 4L, "https://example.com/images/iPhone16.jpg",
                        "최신형 IPhone 18",
                        "결제 완료", BigDecimal.valueOf(1200000.0), BigDecimal.valueOf(197000.0),
                        BigDecimal.valueOf(1500000.0), BigDecimal.valueOf(197000.0), "입찰자A",
                        BigDecimal.valueOf(197000.0),
                        now.minusSeconds(17), now.minusSeconds(17), now.plusDays(7).toString(),
                        "판매자"),

                // 낙찰 (auction_id: 5)
                new MyPageBidHistory(5L, 5L, "https://example.com/images/iPhone16.jpg",
                        "최신형 IPhone 19",
                        "낙찰", BigDecimal.valueOf(1200000.0), BigDecimal.valueOf(196000.0),
                        BigDecimal.valueOf(1500000.0), BigDecimal.valueOf(196000.0), "입찰자A",
                        BigDecimal.valueOf(196000.0),
                        now.minusSeconds(16), now.minusSeconds(16), now.plusDays(7).toString(),
                        "판매자"),

                // 결제 실패 (auction_id: 6)
                new MyPageBidHistory(6L, 6L, "https://example.com/images/iPhone16.jpg",
                        "최신형 IPhone 20",
                        "결제 실패", BigDecimal.valueOf(1200000.0), BigDecimal.valueOf(195000.0),
                        BigDecimal.valueOf(1500000.0), BigDecimal.valueOf(195000.0), "입찰자A",
                        BigDecimal.valueOf(195000.0),
                        now.minusSeconds(15), now.minusSeconds(15), now.plusDays(7).toString(),
                        "판매자"),

                // 경매 종료 - 낙찰 실패 (auction_id: 7)
                new MyPageBidHistory(7L, 7L, "https://example.com/images/iPhone16.jpg",
                        "최신형 IPhone 21",
                        "경매 종료", BigDecimal.valueOf(1200000.0), BigDecimal.valueOf(194000.0),
                        BigDecimal.valueOf(1500000.0), BigDecimal.valueOf(194000.0), "입찰자B",
                        BigDecimal.valueOf(194000.0),
                        now.minusSeconds(14), now.minusSeconds(14), "", "판매자"),

                // 경매 종료 - 낙찰 성공 (auction_id: 7)
                new MyPageBidHistory(7L, 8L, "https://example.com/images/iPhone16.jpg",
                        "최신형 IPhone 21",
                        "낙찰", BigDecimal.valueOf(1200000.0), BigDecimal.valueOf(194000.0),
                        BigDecimal.valueOf(1500000.0), BigDecimal.valueOf(194000.0), "입찰자B",
                        BigDecimal.valueOf(194000.0),
                        now.minusSeconds(13), now.minusSeconds(13), "", "판매자"),

                // 진행중인 경매 (auction_id: 8)
                new MyPageBidHistory(8L, 9L, "https://example.com/images/iPhone16.jpg",
                        "최신형 IPhone 22",
                        "진행중", BigDecimal.valueOf(1200000.0), BigDecimal.valueOf(194000.0),
                        BigDecimal.valueOf(1500000.0), BigDecimal.valueOf(194000.0), "",
                        BigDecimal.valueOf(0.0),
                        now.minusSeconds(13), now.minusSeconds(13), "", "판매자")
        );
    }

    private List<MyPageBidHistory> createMockBidHistoriesWithVariousStatuses() {
        LocalDateTime now = LocalDateTime.now();

        return Arrays.asList(
                // 구매확정 (SUCCESS + DELIVERED)
                new MyPageBidHistory(4L, 4L, "https://example.com/images/iPhone16.jpg",
                        "최신형 IPhone 18",
                        "구매확정", BigDecimal.valueOf(1200000.0), BigDecimal.valueOf(197000.0),
                        BigDecimal.valueOf(1500000.0), BigDecimal.valueOf(197000.0), "입찰자A",
                        BigDecimal.valueOf(197000.0),
                        now.minusSeconds(17), now.minusSeconds(17), now.plusDays(7).toString(),
                        "판매자"),

                // 낙찰 (PENDING)
                new MyPageBidHistory(5L, 5L, "https://example.com/images/iPhone16.jpg",
                        "최신형 IPhone 19",
                        "낙찰", BigDecimal.valueOf(1200000.0), BigDecimal.valueOf(196000.0),
                        BigDecimal.valueOf(1500000.0), BigDecimal.valueOf(196000.0), "입찰자A",
                        BigDecimal.valueOf(196000.0),
                        now.minusSeconds(16), now.minusSeconds(16), now.plusDays(7).toString(),
                        "판매자"),

                // 결제 실패 (REJECTED)
                new MyPageBidHistory(6L, 6L, "https://example.com/images/iPhone16.jpg",
                        "최신형 IPhone 20",
                        "결제 실패", BigDecimal.valueOf(1200000.0), BigDecimal.valueOf(195000.0),
                        BigDecimal.valueOf(1500000.0), BigDecimal.valueOf(195000.0), "입찰자A",
                        BigDecimal.valueOf(195000.0),
                        now.minusSeconds(15), now.minusSeconds(15), now.plusDays(7).toString(),
                        "판매자"),

                // 패찰 (FAIL)
                new MyPageBidHistory(3L, 3L, "https://example.com/images/iPhone16.jpg",
                        "최신형 IPhone 17",
                        "패찰", BigDecimal.valueOf(1200000.0), BigDecimal.valueOf(198000.0),
                        BigDecimal.valueOf(1500000.0), BigDecimal.valueOf(198000.0), "",
                        BigDecimal.valueOf(0.0),
                        now.minusSeconds(18), now.minusSeconds(18), "", "판매자")
        );
    }
}
