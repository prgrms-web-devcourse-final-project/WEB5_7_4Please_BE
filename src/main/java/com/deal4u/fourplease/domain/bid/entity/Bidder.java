package com.deal4u.fourplease.domain.bid.entity;

import com.deal4u.fourplease.domain.member.entity.TempMember;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class Bidder {

    @ManyToOne(fetch = FetchType.LAZY)
    private TempMember member;
}
