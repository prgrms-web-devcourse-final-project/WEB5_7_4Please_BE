package com.deal4u.fourplease.domain.bid.entity;

import com.deal4u.fourplease.domain.member.entity.Member;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Bidder {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_member_id")
    private Member member;

    public static Bidder createBidder(Member member) {
        return new Bidder(member);
    }
}
