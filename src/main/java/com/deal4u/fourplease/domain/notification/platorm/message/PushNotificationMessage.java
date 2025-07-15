package com.deal4u.fourplease.domain.notification.platorm.message;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class PushNotificationMessage {

    private final List<Long> memberIds;
    private final PushMessageBody pushMessageBody;

    private PushNotificationMessage(List<Long> memberIds, PushMessageBody pushMessageBody) {
        this.memberIds = memberIds;
        this.pushMessageBody = pushMessageBody;
    }

    public static SimpleMessageBuilder simpleMessageBuilder() {
        return new SimpleMessageBuilder();
    }

    public static UrlMessageBuilder urlMessageBuilder() {
        return new UrlMessageBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SimpleMessageBuilder {

        private final List<Long> memberIds = new ArrayList<>();
        private String message;

        public SimpleMessageBuilder message(String message) {
            this.message = message;
            return this;
        }

        public SimpleMessageBuilder addTo(Long memberId) {
            this.memberIds.add(memberId);
            return this;
        }

        public PushNotificationMessage build() {
            SimpleTextMessageBody body = new SimpleTextMessageBody(message);
            return new PushNotificationMessage(memberIds, body);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UrlMessageBuilder {

        private final List<Long> memberIds = new ArrayList<>();
        private String message;
        private URL url;

        public UrlMessageBuilder message(String message) {
            this.message = message;
            return this;
        }

        public UrlMessageBuilder addTo(Long memberId) {
            this.memberIds.add(memberId);
            return this;
        }

        public UrlMessageBuilder url(URL url) {
            this.url = url;
            return this;
        }

        public PushNotificationMessage build() {
            UrlMessageBody body = new UrlMessageBody(url, message);
            return new PushNotificationMessage(memberIds, body);
        }
    }
}
