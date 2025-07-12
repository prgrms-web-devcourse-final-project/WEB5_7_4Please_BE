package com.deal4u.fourplease.domain.payment.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_PAYMENT_AMOUNT;
import static com.deal4u.fourplease.global.exception.ErrorCode.ORDER_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.PAYMENT_CONFIRMATION_FAILED;
import static com.deal4u.fourplease.global.exception.ErrorCode.PAYMENT_ERROR;

import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.repository.OrderRepository;
import com.deal4u.fourplease.domain.payment.config.TossApiClient;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmRequest;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmResponse;
import com.deal4u.fourplease.domain.payment.entity.Payment;
import com.deal4u.fourplease.domain.payment.mapper.PaymentMapper;
import com.deal4u.fourplease.domain.payment.repository.PaymentRepository;
import com.deal4u.fourplease.global.exception.GlobalException;
import com.deal4u.fourplease.global.lock.NamedLock;
import com.deal4u.fourplease.global.lock.NamedLockProvider;
import feign.FeignException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PAYMENT_SUCCESS = "DONE";

    private final TossApiClient tossApiClient;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final NamedLockProvider namedLockProvider;

    @Transactional
    public void paymentConfirm(TossPaymentConfirmRequest tossPaymentConfirmRequest) {
        OrderId orderId = OrderId.create(tossPaymentConfirmRequest.orderId());

        Order order = findOrderOrThrow(orderId);

        validateAmount(tossPaymentConfirmRequest, order);


        NamedLock lock = getNamedLock(orderId);
        lock.lock();

        try {
            processTossPayment(tossPaymentConfirmRequest, order);
        } catch (FeignException e) {
            throw PAYMENT_ERROR.toException();
        } catch (GlobalException e) {
            throw PAYMENT_CONFIRMATION_FAILED.toException();
        } finally {
            lock.unlock();
        }
    }

    private Order findOrderOrThrow(OrderId orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(ORDER_NOT_FOUND::toException);
    }

    private void validateAmount(TossPaymentConfirmRequest tossPaymentConfirmRequest, Order order) {
        BigDecimal amountFromRequest = new BigDecimal(tossPaymentConfirmRequest.amount());

        if (order.getPrice().compareTo(amountFromRequest) != 0) {
            throw INVALID_PAYMENT_AMOUNT.toException();
        }
    }

    private NamedLock getNamedLock(OrderId orderId) {
        return namedLockProvider.getBottleLock(orderId.toString());
    }

    private void processTossPayment(TossPaymentConfirmRequest tossPaymentConfirmRequest,
                                    Order order) {
        TossPaymentConfirmResponse response =
                tossApiClient.confirmPayment(tossPaymentConfirmRequest);

        validatePaymentSuccess(response);

        createPayment(order, tossPaymentConfirmRequest, response);
    }

    private void validatePaymentSuccess(TossPaymentConfirmResponse response) {
        if (!PAYMENT_SUCCESS.equals(response.status())) {
            throw PAYMENT_CONFIRMATION_FAILED.toException();
        }
    }

    private void createPayment(Order order, TossPaymentConfirmRequest tossPaymentConfirmRequest,
                               TossPaymentConfirmResponse response) {
        Payment payment = PaymentMapper.toPayment(order, tossPaymentConfirmRequest, response);
        paymentRepository.save(payment);
    }
}
