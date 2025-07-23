package com.deal4u.fourplease.domain.settlement.service;

import static com.deal4u.fourplease.global.exception.ErrorCode.EMAIL_SEND_FAILED_TO_SECOND_BIDDER;
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
public class SecondBidderNotifier {

    private final NotificationSender notificationSender;

    public void send(Bid secondHighestBid, Auction auction,
                     LocalDateTime paymentDeadline) {
        sendEmail(secondHighestBid, auction, paymentDeadline);
        sendPushNotification(secondHighestBid, auction);
    }

    private void sendEmail(Bid secondHighestBid, Auction auction, LocalDateTime paymentDeadline) {
        try {
            String bidderEmail = secondHighestBid.getBidder().getMember().getEmail();

            HtmlEmailMessage emailMessage = HtmlEmailMessage.builder()
                    .templateName("offerSecondBidder")
                    .subject("[Deal4U] 차상위 입찰자 결제 안내")
                    .addEmail(bidderEmail)
                    .addData("bidderName", secondHighestBid.getBidder().getMember().getNickName())
                    .addData("auctionTitle", auction.getProduct().getName())
                    .addData("bidAmount", secondHighestBid.getPrice())
                    .addData("paymentDeadline", paymentDeadline)
                    .addData("paymentUrl", "/") // todo: 실제 url로 변경
                    .build();

            notificationSender.send(emailMessage);

        } catch (MailSendException e) {
            throw EMAIL_SEND_FAILED_TO_SECOND_BIDDER.toException(e);
        }
    }

    private void sendPushNotification(Bid secondHighestBid, Auction auction) {
        try {
            String body = auction.getProduct().getName() + " 상품의 결제 기회가 생겼어요!";
            String url = "/mypage/bid-history"; // TODO: 실제 URL로 변경

            PushNotificationMessage pushMessage = PushNotificationMessage.urlMessageBuilder()
                    .type("SECOND_BIDDER_OFFER")
                    .message(body)
                    .url(url)
                    .addReceiver(secondHighestBid.getBidder().getMember().getMemberId())
                    .build();

            notificationSender.send(pushMessage);

        } catch (Exception e) {
            throw PUSH_NOTIFICATION_SEND_FAILED.toException(e);
        }
    }
}
