package com.deal4u.fourplease.domain.order.entity;

import com.deal4u.fourplease.domain.member.entity.Member;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class Orderer {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
}
