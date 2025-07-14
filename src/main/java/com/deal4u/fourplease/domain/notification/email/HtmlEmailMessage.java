package com.deal4u.fourplease.domain.notification.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class HtmlEmailMessage {

    private final String htmlFileName;
    private final String subject;
    private final Map<String, Object> data;
    private final String[] emails;

    private HtmlEmailMessage(String htmlFileName, String subject, Map<String, Object> data,
            String[] emails) {
        this.htmlFileName = htmlFileName;
        this.subject = subject;
        this.data = data;
        this.emails = emails;
    }

    public static EmailMessageBuilder builder() {
        return new EmailMessageBuilder();
    }

    public static class EmailMessageBuilder {

        private String templateName;
        private String subject;
        private Map<String, Object> data = new HashMap<>();
        private List<String> emails = new ArrayList<>();

        private EmailMessageBuilder() {
        }

        public EmailMessageBuilder templateName(String templateName) {
            this.templateName = templateName;
            return this;
        }

        public EmailMessageBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public EmailMessageBuilder addData(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        public EmailMessageBuilder addEmail(String email) {
            this.emails.add(email);
            return this;
        }

        public HtmlEmailMessage build() {
            return new HtmlEmailMessage(templateName, subject, data, emails.toArray(new String[0]));
        }
    }
}
