package com.deal4u.fourplease.domain.bid.entity;

import com.deal4u.fourplease.domain.member.entity.Member;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@Getter
@EqualsAndHashCode
public class Bidder {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_member_id")
    private Member member;

    public Bidder(Member member) {
        this.member = member;
    }

    public Bidder() {

    }
}
