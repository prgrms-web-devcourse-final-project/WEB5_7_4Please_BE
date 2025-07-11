package com.deal4u.fourplease.domain.payment.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_PAYMENT_AMOUNT;
import static com.deal4u.fourplease.global.exception.ErrorCode.ORDER_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.PAYMENT_CONFIRMATION_FAILED;

import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.repository.OrderRepository;
import com.deal4u.fourplease.domain.payment.config.TossApiClient;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmRequest;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmResponse;
import com.deal4u.fourplease.domain.payment.entity.Payment;
import com.deal4u.fourplease.domain.payment.mapper.PaymentMapper;
import com.deal4u.fourplease.domain.payment.repository.PaymentRepository;
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

    @Transactional
    public void paymentConfirm(TossPaymentConfirmRequest tossPaymentConfirmRequest) {

        OrderId orderId = OrderId.create(tossPaymentConfirmRequest.orderId());

        // Todo: 컨텍스트 홀더에서 결제자와 로그인한 유저가 동일한지 검증하는 로직 필요
        // Todo: 락 처리 추가

        Order order = findOrderOrThrow(orderId);

        validateAmount(tossPaymentConfirmRequest, order);

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

    private void validateAmount(TossPaymentConfirmRequest tossPaymentConfirmRequest,
                                Order order) {
        if (!order.getPrice().equals(new BigDecimal(tossPaymentConfirmRequest.amount()))) {
            throw INVALID_PAYMENT_AMOUNT.toException();
        }
    }

    private Order findOrderOrThrow(OrderId orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(ORDER_NOT_FOUND::toException);
    }
}
