package com.deal4u.fourplease.domain.auction.validator;

import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.global.exception.ErrorCode;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Validator {

    public static void validateListNotEmpty(List<?> list) {
        if (list.isEmpty()) {
            throw ErrorCode.EMPTY_LIST.toException();
        }
    }

    public static void validateSeller(Seller seller, Member member) {
        if (!seller.getMember().equals(member)) {
            throw ErrorCode.FORBIDDEN_RECEIVER.toException();
        }
    }

}
