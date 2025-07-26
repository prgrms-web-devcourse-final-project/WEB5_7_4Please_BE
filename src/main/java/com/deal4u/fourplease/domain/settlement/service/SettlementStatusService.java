package com.deal4u.fourplease.domain.settlement.service;

import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementStatusService {

    // 정산 상태를 성공으로 변경
    @Transactional
    public void markSettlementAsSuccess(Settlement settlement) {
        settlement.updateStatus(SettlementStatus.SUCCESS, LocalDateTime.now(), null);
    }

    // 정산 상태를 거절로 변경
    @Transactional
    public void markSettlementAsRejected(Settlement settlement, String rejectedReason) {
        settlement.updateStatus(SettlementStatus.REJECTED, null, rejectedReason);
    }
}
