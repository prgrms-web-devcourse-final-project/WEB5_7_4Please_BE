package com.deal4u.fourplease.domain.settlement.service;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bidder;
import com.deal4u.fourplease.domain.notification.NotificationSender;
import com.deal4u.fourplease.domain.notification.email.HtmlEmailMessage;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HighestBidderNotifier {

    private final NotificationSender notificationSender;

    public void send(Bidder bidder, Auction auction, LocalDateTime paymentDeadline) {
        sendEmail(bidder, auction, paymentDeadline);
        sendPushNotification(bidder, auction);
    }

    private void sendEmail(Bidder bidder, Auction auction, LocalDateTime paymentDeadline) {
        String bidderEmail = bidder.getMember().getEmail();

        HtmlEmailMessage emailMessage = HtmlEmailMessage.builder()
                .templateName("offerHighestBidder")
                .subject("[Deal4U] 경매 낙찰 안내 및 결제 안내")
                .addEmail(bidderEmail)
                .addData("bidderName", bidder.getMember().getNickName())
                .addData("auctionTitle", auction.getProduct().getName())
                .addData("paymentDeadline", paymentDeadline)
                .addData("paymentUrl", "/") // TODO: 내 입찰 내역으로 변경
                .build();

        notificationSender.send(emailMessage);
    }

    private void sendPushNotification(Bidder bidder, Auction auction) {
        String body = auction.getProduct().getName() + " 상품에 낙찰되었습니다. 결제 기한을 확인하세요!";
        String url = "/mypage/bid-history"; // TODO: 내 입찰 내역으로 변경

        PushNotificationMessage pushMessage = PushNotificationMessage.urlMessageBuilder()
                .type("HIGHEST_BIDDER_OFFER")
                .message(body)
                .url(url)
                .addReceiver(bidder.getMember().getMemberId())
                .build();

        notificationSender.send(pushMessage);
    }
}
