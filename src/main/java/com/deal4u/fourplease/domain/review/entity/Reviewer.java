package com.deal4u.fourplease.domain.review.entity;

import com.deal4u.fourplease.domain.member.entity.Member;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Embeddable
public class Reviewer {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member reviewer;

}
