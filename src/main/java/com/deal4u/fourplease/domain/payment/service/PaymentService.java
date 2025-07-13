package com.deal4u.fourplease.domain.payment.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.PAYMENT_CONFIRMATION_FAILED;
import static com.deal4u.fourplease.global.exception.ErrorCode.PAYMENT_ERROR;

import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.payment.config.TossApiClient;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmRequest;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmResponse;
import com.deal4u.fourplease.domain.payment.util.PaymentValidator;
import com.deal4u.fourplease.global.exception.GlobalException;
import com.deal4u.fourplease.global.lock.NamedLock;
import com.deal4u.fourplease.global.lock.NamedLockProvider;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PAYMENT_SUCCESS = "DONE";

    private final TossApiClient tossApiClient;
    private final NamedLockProvider namedLockProvider;
    private final PaymentTransactionService paymentTransactionService;

    public void paymentConfirm(TossPaymentConfirmRequest tossPaymentConfirmRequest) {
        OrderId orderId = OrderId.create(tossPaymentConfirmRequest.orderId());

        Order order = paymentTransactionService.getOrderOrThrow(orderId);

        PaymentValidator.validateAmount(tossPaymentConfirmRequest, order);

        NamedLock lock = getNamedLock(orderId);
        lock.lock();

        try {
            TossPaymentConfirmResponse response = callTossPaymentApi(tossPaymentConfirmRequest);

            PaymentValidator.validatePaymentSuccess(response);

            paymentTransactionService.savePayment(order, tossPaymentConfirmRequest, response);

        } finally {
            lock.unlock();
        }
    }

    private NamedLock getNamedLock(OrderId orderId) {
        return namedLockProvider.getBottleLock(orderId.toString());
    }

    private TossPaymentConfirmResponse callTossPaymentApi(
            TossPaymentConfirmRequest tossPaymentConfirmRequest) {
        try {
            return tossApiClient.confirmPayment(tossPaymentConfirmRequest);
        } catch (FeignException e) {
            throw PAYMENT_ERROR.toException();
        } catch (GlobalException e) {
            throw PAYMENT_CONFIRMATION_FAILED.toException();
        }
    }
}
