package com.deal4u.fourplease.domain.order.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.AUCTION_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_AUCTION_BIDDER;
import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_BID_PRICE;
import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_INSTANT_BID_PRICE;
import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_ORDER_TYPE;
import static com.deal4u.fourplease.global.exception.ErrorCode.ORDER_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.USER_NOT_FOUND;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.repository.AuctionRepository;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.bid.repository.BidRepository;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.domain.order.dto.OrderCreateRequest;
import com.deal4u.fourplease.domain.order.dto.OrderResponse;
import com.deal4u.fourplease.domain.order.dto.OrderUpdateRequest;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.entity.OrderType;
import com.deal4u.fourplease.domain.order.entity.Orderer;
import com.deal4u.fourplease.domain.order.mapper.OrderMapper;
import com.deal4u.fourplease.domain.order.repository.OrderRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final AuctionRepository auctionRepository;
    private final OrderRepository orderRepository;
    private final BidRepository bidRepository;

    @Transactional
    public String saveOrder(Long auctionId, String orderType,
                            OrderCreateRequest orderCreateRequest) {

        OrderType orderTypeEnum = validateType(orderType);

        Long memberId = 1L;
        Member member = getMemberOrThrow(memberId);
        Auction auction = getAuctionOrThrow(auctionId);

        BigDecimal expectedPrice = determineOrderPrice(auction, member, orderTypeEnum);

        validateOrderPrice(BigDecimal.valueOf(orderCreateRequest.price()), expectedPrice,
                orderTypeEnum);

        OrderId orderId = OrderId.generate();
        Orderer orderer = Orderer.createOrderer(member);

        Order order = createOrder(auction, orderer, orderId, expectedPrice);

        orderRepository.save(order);

        return orderId.getOrderId();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        return OrderMapper.toOrderResponse(order);
    }

    @Transactional
    public void updateOrder(Long orderId, OrderUpdateRequest orderUpdateRequest) {
        Order order = getOrderOrThrow(orderId);
        order.updateOrder(orderUpdateRequest);
    }

    private OrderType validateType(String orderType) {
        try {
            return OrderType.fromString(orderType);
        } catch (IllegalArgumentException e) {
            throw INVALID_ORDER_TYPE.toException();
        }
    }

    private void validateOrderPrice(BigDecimal requestPrice, BigDecimal expectedPrice,
                                    OrderType orderType) {
        if (requestPrice.compareTo(expectedPrice) != 0) {
            if (OrderType.BUY_NOW.equals(orderType)) {
                throw INVALID_INSTANT_BID_PRICE.toException();
            } else {
                throw INVALID_BID_PRICE.toException();
            }
        }
    }

    private BigDecimal determineOrderPrice(Auction auction, Member member, OrderType orderType) {
        if (OrderType.BUY_NOW.equals(orderType)) {
            return auction.getInstantBidPrice();
        } else if (OrderType.AWARD.equals(orderType)) {
            return getSuccessFulBidPrice(auction, member);
        }

        throw INVALID_ORDER_TYPE.toException();
    }

    private Order createOrder(Auction auction, Orderer orderer, OrderId orderId,
                              BigDecimal orderPrice) {
        return Order.builder()
                .orderId(orderId)
                .auction(auction)
                .orderer(orderer)
                .price(orderPrice)
                .build();
    }

    private BigDecimal getSuccessFulBidPrice(Auction auction, Member member) {
        return bidRepository.findSuccessFulBid(auction.getAuctionId(), member)
                .map(Bid::getPrice)
                .orElseThrow(INVALID_AUCTION_BIDDER::toException);
    }

    private Auction getAuctionOrThrow(Long auctionId) {
        return auctionRepository.findByAuctionIdAndDeletedFalseAndStatusOpen(auctionId)
                .orElseThrow(AUCTION_NOT_FOUND::toException);
    }

    private Member getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(USER_NOT_FOUND::toException);
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findByIdWithAuctionAndProduct(orderId)
                .orElseThrow(ORDER_NOT_FOUND::toException);
    }
}
