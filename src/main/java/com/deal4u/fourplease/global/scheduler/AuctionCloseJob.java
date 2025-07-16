package com.deal4u.fourplease.global.scheduler;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class AuctionCloseJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long auctionId = dataMap.getLong("auctionId");

        System.out.println("Quartz Job 실행: auctionId " + auctionId + " 정산 시작");


    }
}
