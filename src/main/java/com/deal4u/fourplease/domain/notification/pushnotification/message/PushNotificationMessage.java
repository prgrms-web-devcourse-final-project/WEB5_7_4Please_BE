package com.deal4u.fourplease.domain.notification.pushnotification.message;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class PushNotificationMessage {

    private final List<Long> receiverIds;
    private final String type;
    private final PushMessageBody pushMessageBody;

    private PushNotificationMessage(List<Long> receiverIds, String type,
            PushMessageBody pushMessageBody) {
        this.receiverIds = receiverIds;
        this.type = type;
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

        private final Set<Long> receiverIds = new HashSet<>();
        @Getter
        private String type;
        private String message;

        public SimpleMessageBuilder message(String message) {
            this.message = message;
            return this;
        }

        public SimpleMessageBuilder type(String type) {
            this.type = type;
            return this;
        }

        public SimpleMessageBuilder addReceiver(Long memberId) {
            this.receiverIds.add(memberId);
            return this;
        }

        public PushNotificationMessage build() {
            SimpleTextMessageBody body = new SimpleTextMessageBody(message);
            return new PushNotificationMessage(receiverIds.stream().toList(), type, body);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UrlMessageBuilder {

        private final Set<Long> receiverIds = new HashSet<>();
        private String type;
        private String message;
        private String url;

        public UrlMessageBuilder message(String message) {
            this.message = message;
            return this;
        }

        public UrlMessageBuilder type(String type) {
            this.type = type;
            return this;
        }

        public UrlMessageBuilder addReceiver(Long memberId) {
            this.receiverIds.add(memberId);
            return this;
        }

        public UrlMessageBuilder url(String url) {
            this.url = url;
            return this;
        }

        public PushNotificationMessage build() {
            UrlMessageBody body = new UrlMessageBody(url, message);
            return new PushNotificationMessage(receiverIds.stream().toList(), type, body);
        }
    }
}
