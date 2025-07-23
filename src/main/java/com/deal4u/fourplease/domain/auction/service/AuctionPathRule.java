package com.deal4u.fourplease.domain.auction.service;

public interface AuctionPathRule<T> {

    String createAuctionPath(T data);
}
