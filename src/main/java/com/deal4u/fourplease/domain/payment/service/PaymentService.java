package com.deal4u.fourplease.domain.payment.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_PAYMENT_AMOUNT;
import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_PRICE_NOT_UPPER;
import static com.deal4u.fourplease.global.exception.ErrorCode.PAYMENT_CONFIRMATION_FAILED;
import static com.deal4u.fourplease.global.exception.ErrorCode.PAYMENT_ERROR;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.entity.OrderType;
import com.deal4u.fourplease.domain.order.service.OrderService;
import com.deal4u.fourplease.domain.payment.config.TossApiClient;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmRequest;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmResponse;
import com.deal4u.fourplease.domain.payment.entity.Payment;
import com.deal4u.fourplease.global.lock.NamedLock;
import com.deal4u.fourplease.global.lock.NamedLockProvider;
import feign.FeignException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PAYMENT_SUCCESS = "DONE";
    private static final String LOCK_PREFIX = "auction-lock:";

    private final TossApiClient tossApiClient;
    private final NamedLockProvider namedLockProvider;
    private final PaymentTransactionService paymentTransactionService;
    private final BidRepository bidRepository;
    private final OrderService orderService;
    private final PaymentSuccessNotifier paymentSuccessNotifier;

    public void paymentConfirm(TossPaymentConfirmRequest tossPaymentConfirmRequest) {
        OrderId orderId = OrderId.create(tossPaymentConfirmRequest.orderId());
        Order order = paymentTransactionService.getOrderOrThrow(orderId);
        Auction auction = order.getAuction();

        validateAmount(tossPaymentConfirmRequest, order);

        NamedLock lock = getNamedLock(auction);
        lock.lock();

        validateBuyNowInstanceBidprice(order, auction);

        try {
            TossPaymentConfirmResponse response = callTossPaymentApi(tossPaymentConfirmRequest);

            Payment payment =
                    paymentTransactionService.savePayment(order, tossPaymentConfirmRequest,
                            response, auction);

            validatePaymentSuccessOrToFailed(response, payment, order);

            paymentTransactionService.paymentStatusSuccess(payment, order, auction);
            
            paymentSuccessNotifier.send(payment, order, auction);
        } finally {
            lock.unlock();
        }
    }

    private void validateBuyNowInstanceBidprice(Order order, Auction auction) {
        if (OrderType.BUY_NOW.equals(order.getOrderType())) {
            BigDecimal currentMaxBidPrice = getCurrentMaxBidPrice(auction.getAuctionId());
            validateInstancePriceAndOrderFailed(order, auction, currentMaxBidPrice);
        }
    }

    private void validateAmount(TossPaymentConfirmRequest request, Order order) {
        BigDecimal amountFromRequest = new BigDecimal(request.amount());
        if (order.getPrice().compareTo(amountFromRequest) != 0) {
            orderFailed(order);
            throw INVALID_PAYMENT_AMOUNT.toException();
        }
    }

    private NamedLock getNamedLock(Auction auction) {
        String key = LOCK_PREFIX + auction.getAuctionId();
        return namedLockProvider.getBottleLock(key);
    }

    private TossPaymentConfirmResponse callTossPaymentApi(TossPaymentConfirmRequest request) {
        try {
            return tossApiClient.confirmPayment(request);
        } catch (FeignException e) {
            log.error("토스 API 호출 오류: {}", e.getMessage());
            throw PAYMENT_ERROR.toException();
        } catch (Exception e) {
            log.error("결제 API 호출 중 예상치 못한 오류: {}", e.getMessage());
            throw PAYMENT_CONFIRMATION_FAILED.toException();
        }
    }

    private void validatePaymentSuccessOrToFailed(TossPaymentConfirmResponse response,
                                                  Payment payment,
                                                  Order order
    ) {
        if (!PAYMENT_SUCCESS.equals(response.status())) {
            log.warn("결제 승인 실패. 상태: {}", response.status());
            paymentTransactionService.updatePaymentStatusToFailed(payment, order);
            throw PAYMENT_CONFIRMATION_FAILED.toException();
        }
    }

    private void validateInstancePriceAndOrderFailed(Order order, Auction auction,
                                                     BigDecimal currentMaxBidPrice) {
        if (auction.getInstantBidPrice().compareTo(currentMaxBidPrice) < 0) {
            orderFailed(order);
            throw INVALID_PRICE_NOT_UPPER.toException();
        }
    }

    private BigDecimal getCurrentMaxBidPrice(Long auctionId) {
        return bidRepository.findMaxBidPriceByAuctionId(auctionId)
                .orElse(BigDecimal.ZERO);
    }

    private void orderFailed(Order order) {
        orderService.faliedOrder(order);
    }
}
