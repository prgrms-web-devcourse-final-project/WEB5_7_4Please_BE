package com.deal4u.fourplease.domain.order.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.AUCTION_NOT_FOUND;
import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_AUCTION_BIDDER;
import static com.deal4u.fourplease.global.exception.ErrorCode.INVALID_ORDER_TYPE;
import static com.deal4u.fourplease.global.exception.ErrorCode.USER_NOT_FOUND;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.order.dto.OrderCreateRequest;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.entity.Orderer;
import com.deal4u.fourplease.domain.order.repository.OrderRepository;
import com.deal4u.fourplease.domain.order.repository.TempAuctionRepository;
import com.deal4u.fourplease.domain.order.repository.TempMemberRepository;
import com.deal4u.fourplease.domain.settlement.entity.SettlementStatus;
import com.deal4u.fourplease.domain.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private static final String BUY_NOW = "BUY_NOW";
    private static final String AWARD = "AWARD";

    private final SettlementRepository settlementRepository;
    private final TempMemberRepository tempMemberRepository;
    private final TempAuctionRepository tempAuctionRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public String createOrder(@NonNull Long auctionId, @NonNull String orderType,
                              @NonNull OrderCreateRequest orderCreateRequest) {

        validateType(orderType);

        Auction auction = findAuctionOrThrow(auctionId);
        Member findMember = findMemberOrThrow(orderCreateRequest.memberId());

        OrderId orderId = OrderId.generate();
        Orderer orderer = Orderer.createOrderer(findMember);

        validateBidderForAuction(auction, findMember, orderType);

        Order order = createOrder(orderCreateRequest, auction, orderer, orderId);

        orderRepository.save(order);

        return orderId.getOrderId();
    }

    private void validateType(String orderType) {
        if (!BUY_NOW.equals(orderType) && !AWARD.equals(orderType)) {
            throw INVALID_ORDER_TYPE.toException();
        }
    }

    private void validateBidderForAuction(Auction auction, Member member, String orderType) {
        if (AWARD.equals(orderType) && !isBidderForAuction(auction, member)) {
            throw INVALID_AUCTION_BIDDER.toException();
        }
    }

    private boolean isBidderForAuction(Auction auction, Member member) {
        Bidder bidder = Bidder.createBidder(member);

        return settlementRepository.existsByAuctionAndBidderAndStatus(
                auction, bidder, SettlementStatus.PENDING);
    }

    private Order createOrder(OrderCreateRequest orderCreateRequest, Auction auction,
                              Orderer orderer, OrderId orderId) {
        return OrderCreateRequest.toEntity(orderCreateRequest, auction, orderer, orderId);
    }

    private Auction findAuctionOrThrow(Long auctionId) {
        return tempAuctionRepository.findByAuctionIdAndDeletedFalse(auctionId)
                .orElseThrow(AUCTION_NOT_FOUND::toException);
    }

    private Member findMemberOrThrow(Long memberId) {
        return tempMemberRepository.findById(memberId)
                .orElseThrow(USER_NOT_FOUND::toException);
    }
}
