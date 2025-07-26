package com.deal4u.fourplease.domain.payment.service;

import com.deal4u.fourplease.domain.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentStatusService {

    // 결제 실패 상태로 변경
    @Transactional
    public void markPaymentAsFailed(Payment payment) {
        payment.statusFailed();
    }

    // 결제 성공 상태로 변경
    @Transactional
    public void markPaymentAsSuccess(Payment payment) {
        payment.statusSuccess();
    }
}
