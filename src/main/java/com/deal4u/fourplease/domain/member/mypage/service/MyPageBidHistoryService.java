package com.deal4u.fourplease.domain.member.mypage.service;

import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageBidHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageBidHistoryService {

    private final BidRepository bidRepository;

    @Transactional(readOnly = true)
    public PageResponse<MyPageBidHistory> getMyBidHistory(Pageable pageable) {
        Long memberId = 2L;

        Page<MyPageBidHistory> page = bidRepository.findMyBidHistoryH2(memberId, pageable);

        return PageResponse.fromPage(page);
    }
}
