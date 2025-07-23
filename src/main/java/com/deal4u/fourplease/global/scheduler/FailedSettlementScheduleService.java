package com.deal4u.fourplease.global.scheduler;

import static com.deal4u.fourplease.global.exception.ErrorCode.FAILED_SETTLEMENT_SCHEDULE_CANCEL_ERROR;
import static com.deal4u.fourplease.global.exception.ErrorCode.FAILED_SETTLEMENT_SCHEDULE_ERROR;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class FailedSettlementScheduleService {

    private static final String JOB_GROUP = "settlement-jobs";
    private static final String TRIGGER_GROUP = "settlement-triggers";

    private final Scheduler scheduler;

    public void scheduleFailedSettlement(Long settlementId, LocalDateTime executionTime) {
        try {
            JobDetail jobDetail = buildJobDetail(settlementId);
            Trigger trigger = buildTrigger(jobDetail, executionTime, settlementId);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            throw FAILED_SETTLEMENT_SCHEDULE_ERROR.toException();
        }
    }

    private JobDetail buildJobDetail(Long settlementId) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("settlementId", settlementId);

        return JobBuilder.newJob(FailedSettlementJob.class)
                .withIdentity("failedSettlementJob_" + settlementId, JOB_GROUP)
                .withDescription("Job for handling failed settlement: " + settlementId)
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, LocalDateTime executionTime,
                                 Long settlementId) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("trigger_" + settlementId, TRIGGER_GROUP)
                .withDescription("Trigger for settlement: " + settlementId)
                .startAt(Date.from(executionTime.atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();
    }

    public void cancelFailedSettlement(Long settlementId) {
        try {
            scheduler.deleteJob(
                    new JobKey("failedSettlementJob_" + settlementId, JOB_GROUP));
        } catch (Exception e) {
            throw FAILED_SETTLEMENT_SCHEDULE_CANCEL_ERROR.toException();
        }
    }
}
