package com.deal4u.fourplease.global.scheduler;

import com.deal4u.fourplease.domain.auction.service.AuctionService;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.service.BidService;
import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementCloseJob extends QuartzJobBean {

    private final SettlementService settlementService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long settlementId = dataMap.getLong("settlementId");
        settlementService.expireSettlement(settlementId);
    }

}
