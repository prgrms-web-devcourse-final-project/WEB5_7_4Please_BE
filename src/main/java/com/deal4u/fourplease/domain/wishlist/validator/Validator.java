package com.deal4u.fourplease.domain.wishlist.validator;

import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.wishlist.entity.Wishlist;
import com.deal4u.fourplease.global.exception.ErrorCode;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Validator {

    public static void validateMember(Wishlist wishlist, Member member) {
        if (!wishlist.getMemberId().equals(member.getMemberId())) {
            throw ErrorCode.FORBIDDEN_RECEIVER.toException();
        }
    }

}
