package com.deal4u.fourplease.domain.auction.rules;

import com.deal4u.fourplease.domain.auction.service.AuctionImageNameRule;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class DateTimeAuctionImageNameRule implements AuctionImageNameRule {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");

    @Override
    public String createAuctionPath() {
        return formatter.format(LocalDateTime.now());
    }
}
