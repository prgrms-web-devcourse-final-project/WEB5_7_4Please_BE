package com.deal4u.fourplease.domain.auction.validator;

import static com.deal4u.fourplease.global.exception.ErrorCode.AUCTION_CAN_NOT_DELETE;

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

    // 경매 삭제가 가능한 경매 상태인지 확인하는 메서드
    public static void validateAuctionStatus(String auctionStatus) {
        List<String> undeletableStatuses =
                List.of("PENDING", "SUCCESS", "REJECTED", "INTRANSIT", "DELIVERED");
        if (undeletableStatuses.contains(auctionStatus)) {
            throw AUCTION_CAN_NOT_DELETE.toException();
        }
    }

}
