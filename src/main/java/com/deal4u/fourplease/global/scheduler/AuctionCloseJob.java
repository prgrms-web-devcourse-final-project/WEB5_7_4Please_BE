package com.deal4u.fourplease.global.scheduler;

import com.deal4u.fourplease.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionCloseJob extends QuartzJobBean {

    private final SettlementService settlementService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long auctionId = dataMap.getLong("auctionId");
        settlementService.save(auctionId, 1);
    }
}
