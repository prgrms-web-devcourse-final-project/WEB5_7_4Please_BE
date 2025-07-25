package com.deal4u.fourplease.domain.auction.entity;

import com.deal4u.fourplease.domain.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public static Seller create(Member member) {
        return new Seller(member);
    }
}
