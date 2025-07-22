package com.deal4u.fourplease.domain.wishlist.mapper;

import org.springframework.data.domain.Sort;

public class WishlistMapper {

    public static Sort getSort(String order) {
        if (order.equals("newest")) {
            return Sort.by(Sort.Direction.ASC, "createdAt");
        } else {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

}
