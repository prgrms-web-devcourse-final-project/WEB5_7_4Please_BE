package com.deal4u.fourplease.domain.auction.entity;

import com.deal4u.fourplease.domain.member.entity.TempMember;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller {

    // TODO: 병합 후 cascade = CascadeType.PERSIST 삭제 필요
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private TempMember member;

    public Seller(TempMember member) {
        this.member = member;
    }
}
