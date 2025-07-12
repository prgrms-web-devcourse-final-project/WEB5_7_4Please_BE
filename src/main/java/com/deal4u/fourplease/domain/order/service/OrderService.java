package com.deal4u.fourplease.domain.order.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.AUCTION_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_AUCTION_BIDDER;
import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_ORDER_TYPE;
import static com.deal4u.fourplease.global.exception.ErrorCode.ORDER_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.USER_NOT_FOUND;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.order.dto.OrderCreateRequest;
import com.deal4u.fourplease.domain.order.dto.OrderResponse;
import com.deal4u.fourplease.domain.order.dto.OrderUpdateRequest;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.entity.Orderer;
import com.deal4u.fourplease.domain.order.mapper.OrderMapper;
import com.deal4u.fourplease.domain.order.repository.OrderRepository;
import com.deal4u.fourplease.domain.order.repository.TempAuctionRepository;
import com.deal4u.fourplease.domain.order.repository.TempBidRepository;
import com.deal4u.fourplease.domain.order.repository.TempMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String BUY_NOW = "BUY_NOW";
    private static final String AWARD = "AWARD";

    private final TempMemberRepository memberRepository;
    private final TempAuctionRepository auctionRepository;
    private final OrderRepository orderRepository;
    private final TempBidRepository bidRepository;

    @Transactional
    public String saveOrder(Long auctionId, String orderType,
                            OrderCreateRequest orderCreateRequest) {

        validateType(orderType);

        Member member = findMemberOrThrow(orderCreateRequest.memberId());
        Auction auction = findAuctionOrThrow(auctionId);

        validateSuccessfulBidder(auction, member, orderType);

        OrderId orderId = OrderId.generate();
        Orderer orderer = Orderer.createOrderer(member);

        Order order = createOrder(orderCreateRequest, auction, orderer, orderId);

        orderRepository.save(order);

        return orderId.getOrderId();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        return OrderMapper.toOrderResponse(order);
    }

    @Transactional
    public void updateOrder(Long orderId, OrderUpdateRequest orderUpdateRequest) {
        Order order = findOrderOrThrow(orderId);
        order.updateOrder(orderUpdateRequest);
    }

    private void validateType(String orderType) {
        if (!BUY_NOW.equals(orderType) && !AWARD.equals(orderType)) {
            throw INVALID_ORDER_TYPE.toException();
        }
    }

    private void validateSuccessfulBidder(Auction auction, Member member, String orderType) {
        if (AWARD.equals(orderType) && !isSuccessfulBidder(auction, member)) {
            throw INVALID_AUCTION_BIDDER.toException();
        }
    }

    private boolean isSuccessfulBidder(Auction auction, Member member) {
        return bidRepository.existsSuccessfulBidder(
                auction.getAuctionId(), member);
    }

    private Order createOrder(OrderCreateRequest orderCreateRequest, Auction auction,
                              Orderer orderer, OrderId orderId) {
        return OrderCreateRequest.toEntity(orderCreateRequest, auction, orderer, orderId);
    }

    private Auction findAuctionOrThrow(Long auctionId) {
        return auctionRepository.findByAuctionIdAndDeletedFalseAndStatusClosed(auctionId)
                .orElseThrow(AUCTION_NOT_FOUND::toException);
    }

    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(USER_NOT_FOUND::toException);
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findByIdWithAuctionAndProduct(orderId)
                .orElseThrow(ORDER_NOT_FOUND::toException);
    }
}
