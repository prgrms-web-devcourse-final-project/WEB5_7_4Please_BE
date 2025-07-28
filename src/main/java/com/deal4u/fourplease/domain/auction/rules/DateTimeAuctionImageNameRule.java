package com.deal4u.fourplease.domain.auction.rules;

import com.deal4u.fourplease.domain.auction.service.AuctionImageNameRule;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class DateTimeAuctionImageNameRule implements AuctionImageNameRule {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
    private static final SecureRandom random = new SecureRandom();

    @Override
    public String createAuctionPath() {
        String format = formatter.format(LocalDateTime.now());
        return format + random.nextInt();
    }
}
