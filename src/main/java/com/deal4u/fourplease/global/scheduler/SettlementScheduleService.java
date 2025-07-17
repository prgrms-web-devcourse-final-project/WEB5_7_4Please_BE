package com.deal4u.fourplease.global.scheduler;

import com.deal4u.fourplease.domain.settlement.entity.Settlement;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.stereotype.Service;

@Service
public class SettlementScheduleService {

    private final Scheduler scheduler;

    public SettlementScheduleService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    // 정산 시작 시 해당 `Method`를 호출
    public void scheduleSettlementClose(Long settlementId) {
        try {
            JobDetail jobDetail = buildJobDetail(settlementId);
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

}
