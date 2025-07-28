package com.deal4u.fourplease.domain.payment.service;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.notification.NotificationSender;
import com.deal4u.fourplease.domain.notification.email.HtmlEmailMessage;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import com.deal4u.fourplease.domain.order.entity.Order;
import com.deal4u.fourplease.domain.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSuccessNotifier {

    private final NotificationSender notificationSender;

    public void send(Payment payment, Order order, Auction auction) {
        sendEmail(payment, order, auction);
        sendPushNotification(order, auction);
    }

    private void sendEmail(Payment payment, Order order, Auction auction) {
        String buyerEmail = order.getOrderer().getMember().getEmail();

        HtmlEmailMessage emailMessage = HtmlEmailMessage.builder()
                .templateName("paymentSuccess")
                .subject("[Deal4U] 결제 완료 안내")
                .addEmail(buyerEmail)
                .addData("buyerName", order.getOrderer().getMember().getNickName())
                .addData("auctionTitle", auction.getProduct().getName())
                .addData("paymentAmount", payment.getAmount())
                .addData("orderDetailsUrl",
                        "/mypage/order-history") // TODO: 실제 내입찰 내역 url로 변경 필요
                .build();

        notificationSender.send(emailMessage);
    }

    private void sendPushNotification(Order order, Auction auction) {
        String body = auction.getProduct().getName() + " 상품 결제가 완료되었습니다!";
        String url = "/mypage/order-history"; // TODO: 실제 내입찰 내역 url로 변경 필요

        PushNotificationMessage pushMessage = PushNotificationMessage.urlMessageBuilder()
                .type("PAYMENT_SUCCESS")
                .message(body)
                .url(url)
                .addReceiver(order.getOrderer().getMember().getMemberId())
                .build();

        notificationSender.send(pushMessage);
    }
}
