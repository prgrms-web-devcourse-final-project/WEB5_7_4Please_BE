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
import com.deal4u.fourplease.global.exception.GlobalException;
import com.deal4u.fourplease.global.lock.NamedLock;
import com.deal4u.fourplease.global.lock.NamedLockProvider;
import feign.FeignException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public void paymentConfirm(TossPaymentConfirmRequest tossPaymentConfirmRequest) {
        OrderId orderId = OrderId.create(tossPaymentConfirmRequest.orderId());
        Order order = paymentTransactionService.getOrderOrThrow(orderId);
        Auction auction = order.getAuction();
        validateAmount(tossPaymentConfirmRequest, order);

        NamedLock lock = getNamedLock(auction);
        lock.lock();

        Payment payment = null;
        boolean shouldCloseAuction = true;

        try {
            TossPaymentConfirmResponse response = callTossPaymentApi(tossPaymentConfirmRequest);
            validatePaymentSuccess(response);

            payment = paymentTransactionService.savePayment(order, tossPaymentConfirmRequest,
                    response, auction);

            if (order.getOrderType().equals(OrderType.BUY_NOW)) {
                BigDecimal currentMaxBidPrice = getCurrentMaxBidPrice(auction.getAuctionId());

                shouldCloseAuction = validateInstantBidPrice(auction, currentMaxBidPrice);

                if (!shouldCloseAuction) {
                    paymentTransactionService.updatePaymentStatusToFailed(payment, order);
                }
            }

        } catch (GlobalException e) {
            if (payment != null) {
                paymentTransactionService.updatePaymentStatusToFailed(payment, order);
            }
        } finally {
            if (shouldCloseAuction) {
                orderService.closeAuction(auction);
            }
            lock.unlock();
        }
    }

    private boolean validateInstantBidPrice(Auction auction, BigDecimal currentMaxBidPrice) {
        try {
            validateInstancePrice(auction, currentMaxBidPrice);
            return true;
        } catch (GlobalException e) {
            return false;
        }
    }

    private void validateAmount(TossPaymentConfirmRequest request, Order order) {
        BigDecimal amountFromRequest = new BigDecimal(request.amount());
        if (order.getPrice().compareTo(amountFromRequest) != 0) {
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
            throw PAYMENT_ERROR.toException();
        } catch (GlobalException e) {
            throw PAYMENT_CONFIRMATION_FAILED.toException();
        }
    }

    private void validatePaymentSuccess(TossPaymentConfirmResponse response) {
        if (!PAYMENT_SUCCESS.equals(response.status())) {
            throw PAYMENT_CONFIRMATION_FAILED.toException();
        }
    }

    private static void validateInstancePrice(Auction auction, BigDecimal currentMaxBidPrice) {
        if (auction.getInstantBidPrice().compareTo(currentMaxBidPrice) < 0) {
            throw INVALID_PRICE_NOT_UPPER.toException();
        }
    }

    private BigDecimal getCurrentMaxBidPrice(Long auctionId) {
        return bidRepository.findMaxBidPriceByAuctionId(auctionId).orElse(BigDecimal.ZERO);
    }
}
