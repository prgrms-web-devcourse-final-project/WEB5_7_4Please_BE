package com.deal4u.fourplease.domain.payment.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.ORDER_NOT_FOUND;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionStatus;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.repository.OrderRepository;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmRequest;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmResponse;
import com.deal4u.fourplease.domain.payment.entity.Payment;
import com.deal4u.fourplease.domain.payment.mapper.PaymentMapper;
import com.deal4u.fourplease.domain.payment.repository.PaymentRepository;
import com.deal4u.fourplease.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class PaymentTransactionService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SettlementService settlementService;

    @Transactional(readOnly = true)
    public Order getOrderOrThrow(OrderId orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(ORDER_NOT_FOUND::toException);
    }

    @Transactional
    public Payment savePayment(Order order,
                               TossPaymentConfirmRequest req,
                               TossPaymentConfirmResponse resp,
                               Auction auction
    ) {
        Payment payment = PaymentMapper.toPayment(order, req, resp);
        return paymentRepository.save(payment);
    }

    @Transactional
    public void updatePaymentStatusToFailed(Payment payment, Order order) {
        payment.statusFailed();
        order.failed();
    }

    @Transactional
    public void paymentStatusSuccess(Payment payment, Order order, Auction auction) {
        if (auction.getStatus().equals(AuctionStatus.OPEN)) {
            auction.close();
        }
        order.success();
        payment.statusSuccess();
        settlementService.chagneSettlementSucess(auction);
    }
}
