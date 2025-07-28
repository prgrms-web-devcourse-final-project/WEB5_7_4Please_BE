package com.deal4u.fourplease.domain.order.service;

import com.deal4u.fourplease.domain.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderStatusService {

    // 주문 상태를 성공으로 변경
    @Transactional
    public void markOrderAsSuccess(Order order) {
        order.success();
    }

    // 주문 상태를 실패로 변경
    @Transactional
    public void markOrderAsFailed(Order order) {
        order.failed();
    }
}
