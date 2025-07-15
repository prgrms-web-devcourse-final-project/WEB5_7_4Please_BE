package com.deal4u.fourplease.domain.auction.rules;

import com.deal4u.fourplease.domain.auction.service.AuctionPathRule;
import org.springframework.stereotype.Component;

@Component
public class NickNameAuctionPathRule implements AuctionPathRule<String> {

    private static final String PREFIX = "/image";

    @Override
    public String createAuctionPath(String nickName) {
        return PREFIX + "/" + nickName;
    }
}
