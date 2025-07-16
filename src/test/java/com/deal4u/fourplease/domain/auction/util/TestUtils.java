package com.deal4u.fourplease.domain.auction.util;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.entity.Address;
import com.deal4u.fourplease.domain.auction.entity.BidPeriod;
import com.deal4u.fourplease.domain.auction.entity.Category;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.ProductImage;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TestUtils {

    public static ProductCreateDto genProductCreateDto() {
        return new ProductCreateDto(
                genMember(),
                "칫솔",
                "한 번도 사용하지 않은 새 칫솔 입니다. 치약은 없습니다.",
                "http://example.com/thumbnail.jpg",
                List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg"),
                4L,
                "서울시 강남구",
                "101동 102호",
                "000000",
                "010-0000-0000"
        );
    }

    public static Member genMember() {
        // TODO: Member 추후 확인 필요
        return Member.builder()
                .memberId(1L)
                .email("user1@user.com")
                .nickName("유저1")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
    }

    public static Product genProduct() {
        return Product.builder()
                .name("칫솔")
                .description("한 번도 사용하지 않은 새 칫솔 입니다. 치약은 없습니다.")
                .thumbnailUrl("http://example.com/thumbnail.jpg")
                .address(new Address(
                        "서울시 강남구",
                        "101동 102호",
                        "000000"
                ))
                .category(new Category(4L, "생활용품"))
                .phone("010-0000-0000")
                .build();
    }

    public static AuctionCreateRequest genAuctionCreateRequest() {
        return new AuctionCreateRequest(
                "칫솔",
                "한 번도 사용하지 않은 새 칫솔 입니다. 치약은 없습니다.",
                "http://example.com/thumbnail.jpg",
                List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg"),
                4L,
                "서울시 강남구",
                "101동 102호",
                "000000",
                "010-0000-0000",
                LocalDateTime.now(),
                BidPeriod.THREE,
                BigDecimal.valueOf(100_000),
                null
        );
    }

    public static AuctionDetailResponse genAuctionDetailResponse() {
        return new AuctionDetailResponse(
                BigDecimal.valueOf(200_0000),
                null,
                20,
                BigDecimal.valueOf(100_0000),
                "칫솔",
                4L,
                "생활용품",
                "한 번도 사용하지 않은 새 칫솔 입니다. 치약은 없습니다.",
                LocalDateTime.now(),
                "http://example.com/thumbnail.jpg",
                List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg")
        );
    }

    public static List<ProductImage> genProductImageList(Product product) {
        return List.of(
                new ProductImage(1L, product, "http://example.com/image1.jpg"),
                new ProductImage(2L, product, "http://example.com/image2.jpg")
        );
    }
}
