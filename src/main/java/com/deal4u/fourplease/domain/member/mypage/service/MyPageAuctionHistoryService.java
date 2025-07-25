package com.deal4u.fourplease.domain.member.mypage.service;

import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.mypage.dto.MyPageAuctionHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageAuctionHistoryService {

    public PageResponse<MyPageAuctionHistory> getMyAuctionHistory(Pageable pageable) {
        return null;
    }
}
