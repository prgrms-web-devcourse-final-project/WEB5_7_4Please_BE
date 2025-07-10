package com.deal4u.fourplease.domain.order.repository;

import com.deal4u.fourplease.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
