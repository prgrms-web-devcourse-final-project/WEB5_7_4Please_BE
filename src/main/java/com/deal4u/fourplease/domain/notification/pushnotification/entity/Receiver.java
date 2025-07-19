package com.deal4u.fourplease.domain.notification.pushnotification.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
public class Receiver {

    private Long memberId;

    public static Receiver of(Long mebmerId) {
        Receiver receiver = new Receiver();
        receiver.memberId = mebmerId;
        return receiver;
    }
}
