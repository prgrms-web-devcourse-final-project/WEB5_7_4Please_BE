package com.deal4u.fourplease.global.scheduler;

import static com.deal4u.fourplease.global.exception.ErrorCode.SETTLEMENT_NOT_FOUND;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

@Service
public class SettlementScheduleService {

    private final Scheduler scheduler;

    public SettlementScheduleService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    // 정산 시작 시 해당 `Method`를 호출
    public void scheduleSettlementClose(Long settlementId, LocalDateTime paymentDeadline) {
        try {
            JobDetail jobDetail = buildJobDetail(settlementId);
            Trigger trigger = buildTrigger(jobDetail, paymentDeadline);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            throw SETTLEMENT_NOT_FOUND.toException();
        }
    }

    private JobDetail buildJobDetail(Long settlementId) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("settlementId", settlementId);

        return JobBuilder.newJob(SettlementCloseJob.class)
                .withIdentity("settlementCloseJob_" + settlementId, "settlement-jobs")
                .withDescription("Settlement closing job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, LocalDateTime paymentDeadline) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "settlement-triggers")
                .withDescription("Settlement closing trigger")
                .startAt(Date.from(paymentDeadline.atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();
    }

}
