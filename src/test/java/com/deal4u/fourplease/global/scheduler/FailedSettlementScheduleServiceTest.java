package com.deal4u.fourplease.global.scheduler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

import com.deal4u.fourplease.global.exception.GlobalException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class FailedSettlementScheduleServiceTest {

    @Mock
    private Scheduler scheduler;

    @InjectMocks
    private FailedSettlementScheduleService failedSettlementScheduleService;

    @Test
    @DisplayName("실패한 정산 스케줄을 등록한다")
    void scheduleFailedSettlement() throws SchedulerException {
        // given
        Long settlementId = 1L;
        LocalDateTime executionTime = LocalDateTime.now().plusHours(48);

        // when
        failedSettlementScheduleService.scheduleFailedSettlement(settlementId, executionTime);

        // then
        ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);

        then(scheduler).should().scheduleJob(jobDetailCaptor.capture(), triggerCaptor.capture());

        JobDetail capturedJobDetail = jobDetailCaptor.getValue();
        Trigger capturedTrigger = triggerCaptor.getValue();

        // JobDetail 검증
        assertThat(capturedJobDetail.getKey().getName()).isEqualTo(
                "failedSettlementJob_" + settlementId);
        assertThat(capturedJobDetail.getKey().getGroup()).isEqualTo("settlement-jobs");
        assertThat(capturedJobDetail.getJobClass()).isEqualTo(FailedSettlementJob.class);
        assertThat(capturedJobDetail.getDescription()).isEqualTo(
                "Job for handling failed settlement: " + settlementId);
        assertThat(capturedJobDetail.getJobDataMap().get("settlementId")).isEqualTo(settlementId);
        assertThat(capturedJobDetail.isDurable()).isTrue();

        // Trigger 검증
        assertThat(capturedTrigger.getKey().getName()).isEqualTo("trigger_" + settlementId);
        assertThat(capturedTrigger.getKey().getGroup()).isEqualTo("settlement-triggers");
        assertThat(capturedTrigger.getDescription()).isEqualTo(
                "Trigger for settlement: " + settlementId);
        assertThat(capturedTrigger.getJobKey()).isEqualTo(capturedJobDetail.getKey());
    }

    @Test
    @DisplayName("스케줄 등록 중 예외가 발생하면 GlobalException을 던진다")
    void scheduleFailedSettlementSchedulerException() throws SchedulerException {
        // given
        Long settlementId = 1L;
        LocalDateTime executionTime = LocalDateTime.now().plusHours(48);

        doThrow(new SchedulerException("Scheduler error"))
                .when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

        // when & then
        assertThatThrownBy(
                () -> failedSettlementScheduleService.scheduleFailedSettlement(settlementId,
                        executionTime))
                .isInstanceOf(GlobalException.class)
                .hasMessage("실패한 정산 작업을 스케줄링하는 중 오류가 발생했습니다.")
                .extracting("status")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("실패한 정산 스케줄을 취소한다")
    void cancelFailedSettlement() throws SchedulerException {
        // given
        Long settlementId = 1L;
        given(scheduler.deleteJob(any(JobKey.class))).willReturn(true);

        // when
        failedSettlementScheduleService.cancelFailedSettlement(settlementId);

        // then
        ArgumentCaptor<JobKey> jobKeyCaptor = ArgumentCaptor.forClass(JobKey.class);
        then(scheduler).should().deleteJob(jobKeyCaptor.capture());

        JobKey capturedJobKey = jobKeyCaptor.getValue();
        assertThat(capturedJobKey.getName()).isEqualTo("failedSettlementJob_" + settlementId);
        assertThat(capturedJobKey.getGroup()).isEqualTo("settlement-jobs");
    }

    @Test
    @DisplayName("스케줄 취소 중 예외가 발생하면 GlobalException을 던진다")
    void cancelFailedSettlementSchedulerException() throws SchedulerException {
        // given
        Long settlementId = 1L;

        doThrow(new SchedulerException("Scheduler error"))
                .when(scheduler).deleteJob(any(JobKey.class));

        // when & then
        assertThatThrownBy(
                () -> failedSettlementScheduleService.cancelFailedSettlement(settlementId))
                .isInstanceOf(GlobalException.class)
                .hasMessage("실패한 정산 작업을 취소하는 중 오류가 발생했습니다.")
                .extracting("status")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("여러 정산 스케줄을 등록할 수 있다")
    void scheduleMultipleFailedSettlements() throws SchedulerException {
        // given
        Long settlementId1 = 1L;
        Long settlementId2 = 2L;
        LocalDateTime executionTime1 = LocalDateTime.now().plusHours(48);
        LocalDateTime executionTime2 = LocalDateTime.now().plusHours(24);

        // when
        failedSettlementScheduleService.scheduleFailedSettlement(settlementId1, executionTime1);
        failedSettlementScheduleService.scheduleFailedSettlement(settlementId2, executionTime2);

        // then
        then(scheduler).should(times(2)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("정산 스케줄 등록 후 취소할 수 있다")
    void scheduleAndCancelFailedSettlement() throws SchedulerException {
        // given
        Long settlementId = 1L;
        LocalDateTime executionTime = LocalDateTime.now().plusHours(48);
        given(scheduler.deleteJob(any(JobKey.class))).willReturn(true);

        // when
        failedSettlementScheduleService.scheduleFailedSettlement(settlementId, executionTime);
        failedSettlementScheduleService.cancelFailedSettlement(settlementId);

        // then
        then(scheduler).should().scheduleJob(any(JobDetail.class), any(Trigger.class));
        then(scheduler).should().deleteJob(any(JobKey.class));
    }
}
