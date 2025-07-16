package com.deal4u.fourplease.global.scheduler;

import com.deal4u.fourplease.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

@Service
public class AuctionScheduleService {

    private final Scheduler scheduler;

    public AuctionScheduleService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    // 경매 시작 시 해당 `Method`를 호출
    public void scheduleAuctionClose(Long auctionId, LocalDateTime endTime) {
        try {
            JobDetail jobDetail = buildJobDetail(auctionId);
            Trigger trigger = buildTrigger(jobDetail, endTime);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            throw ErrorCode.AUCTION_SCHEDULE_ALREADY_EXISTS.toException();
        }
    }

    private JobDetail buildJobDetail(Long auctionId) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("auctionId", auctionId);

        return JobBuilder.newJob(AuctionCloseJob.class)
                .withIdentity("auctionCloseJob_" + auctionId, "auction-jobs")
                .withDescription("Auction closing job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, LocalDateTime startTime) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "auction-triggers")
                .withDescription("Auction closing trigger")
                .startAt(Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();
    }

    // 경매 취소 시 해당 `Method`를 호출
    public void cancelAuctionClose(Long auctionId) {
        try {
            scheduler.deleteJob(new JobKey("auctionCloseJob_" + auctionId, "auction-jobs"));
        } catch (Exception e) {
            throw ErrorCode.AUCTION_SCHEDULE_ALREADY_EXISTS.toException();

        }
    }

}
