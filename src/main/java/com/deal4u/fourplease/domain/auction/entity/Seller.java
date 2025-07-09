package com.deal4u.fourplease.domain.auction.entity;

import com.deal4u.fourplease.domain.member.entity.Member;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Embeddable
public class Seller {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member seller;

}
