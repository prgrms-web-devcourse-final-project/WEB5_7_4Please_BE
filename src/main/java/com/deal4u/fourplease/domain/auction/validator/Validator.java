package com.deal4u.fourplease.domain.auction.validator;

import com.deal4u.fourplease.global.exception.ErrorCode;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Validator {

    public static void validateListNotEmpty(List<?> list) {
        if (list.isEmpty()) {
            throw ErrorCode.EMPTY_LIST.toException();
        }
    }

}
