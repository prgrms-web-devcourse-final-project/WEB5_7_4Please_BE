package com.deal4u.fourplease.domain.settlement.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.EMAIL_SEND_FAILED_TO_HIGHEST_BIDDER;
import static com.deal4u.fourplease.global.exception.ErrorCode.PUSH_NOTIFICATION_SEND_FAILED;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.bid.entity.Bid;
import com.deal4u.fourplease.domain.notification.NotificationSender;
import com.deal4u.fourplease.domain.notification.email.HtmlEmailMessage;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HighestBidderNotifier {

    private final NotificationSender notificationSender;

    public void send(Bid highestBid, Auction auction, LocalDateTime paymentDeadline) {
        sendEmail(highestBid, auction, paymentDeadline);
        sendPushNotification(highestBid, auction);
    }

    private void sendEmail(Bid highestBid, Auction auction, LocalDateTime paymentDeadline) {
        try {
            String bidderEmail = highestBid.getBidder().getMember().getEmail();

            HtmlEmailMessage emailMessage = HtmlEmailMessage.builder()
                    .templateName("offerHighestBidder")
                    .subject("[Deal4U] 경매 낙찰 안내 및 결제 안내")
                    .addEmail(bidderEmail)
                    .addData("bidderName", highestBid.getBidder().getMember().getNickName())
                    .addData("auctionTitle", auction.getProduct().getName())
                    .addData("bidAmount", highestBid.getPrice())
                    .addData("paymentDeadline", paymentDeadline)
                    .addData("paymentUrl", "/") // TODO: 내 입찰 내역으로 변경
                    .build();

            notificationSender.send(emailMessage);

        } catch (MailSendException e) {
            throw EMAIL_SEND_FAILED_TO_HIGHEST_BIDDER.toException(e);
        }
    }

    private void sendPushNotification(Bid highestBid, Auction auction) {
        try {
            String body = auction.getProduct().getName() + " 상품에 낙찰되었습니다. 결제 기한을 확인하세요!";
            String url = "/mypage/bid-history"; // TODO: 내 입찰 내역으로 변경

            PushNotificationMessage pushMessage = PushNotificationMessage.urlMessageBuilder()
                    .type("HIGHEST_BIDDER_OFFER")
                    .message(body)
                    .url(url)
                    .addReceiver(highestBid.getBidder().getMember().getMemberId())
                    .build();

            notificationSender.send(pushMessage);

        } catch (Exception e) {
            throw PUSH_NOTIFICATION_SEND_FAILED.toException(e);
        }
    }
}
