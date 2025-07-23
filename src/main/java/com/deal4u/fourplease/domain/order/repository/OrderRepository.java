package com.deal4u.fourplease.domain.order.repository;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.order.entity.OrderId;
import com.deal4u.fourplease.domain.order.entity.OrderStatus;
import com.deal4u.fourplease.domain.order.entity.Orderer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(OrderId orderId);

    @Query("SELECT o "
            + "FROM Order o "
            + "JOIN FETCH o.auction a "
            + "JOIN FETCH a.product p "
            + "WHERE o.id = :orderId")
    Optional<Order> findByIdWithAuctionAndProduct(@Param("orderId") Long orderId);

    Optional<Order> findByOrdererAndAuctionAndStatus(Orderer orderer, Auction auction,
            OrderStatus status);
}
