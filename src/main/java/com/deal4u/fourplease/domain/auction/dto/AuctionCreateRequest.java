package com.deal4u.fourplease.domain.auction.dto;

import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionDuration;
import com.deal4u.fourplease.domain.auction.entity.BidPeriod;
import com.deal4u.fourplease.domain.auction.entity.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AuctionCreateRequest(
        @NotBlank(message = "상품 이름을 입력해 주세요.")
        @Size(min = 1, max = 20, message = "상품 이름은 1자 이상 20자 이하로 입력해주세요.")
        String productName,

        @NotBlank(message = "상품 설명을 입력해 주세요.")
        @Size(min = 1, max = 1000, message = "상품 설명은 1자 이상 1000자 이하로 입력해주세요.")
        String description,

        @NotBlank(message = "상품 썸네일 이미지를 입력해 주세요.")
        String thumbnailUrl,

        @NotEmpty(message = "상품 상세 이미지를 입력해 주세요.")
        List<String> imageUrls,

        @NotNull(message = "카테고리 ID를 입력해 주세요.")
        Long categoryId,

        @NotBlank(message = "주소를 입력해 주세요.")
        String address,

        @NotBlank(message = "상세 주소를 입력해 주세요.")
        String addressDetail,

        @NotBlank(message = "우편 번호를 입력해 주세요.")
        String zipCode,

        @NotBlank(message = "휴대폰 번호를 입력해 주세요.")
        @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
                message = "휴대폰 번호 형식이 올바르지 않습니다.")
        String phone,

        @NotNull(message = "경매 시작 날짜를 입력해 주세요.")
        LocalDateTime startDate,

        @NotNull(message = "경매 기간을 입력해 주세요.")
        BidPeriod bidPeriod,

        @NotNull(message = "경매 시작가를 입력해 주세요.")
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 15, fraction = 0)
        BigDecimal startingPrice,

        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 15, fraction = 0)
        BigDecimal buyNowPrice

) {
    public Auction toEntity(Product product) {
        return Auction.builder()
                .product(product)
                .startingPrice(startingPrice)
                .instantBidPrice(buyNowPrice)
                .duration(new AuctionDuration(startDate, bidPeriod.getEndTime(startDate)))
                .build();
    }

    public ProductCreateDto toProductCreateDto() {
        return new ProductCreateDto(
                productName,
                description,
                thumbnailUrl,
                imageUrls,
                categoryId,
                address,
                addressDetail,
                zipCode,
                phone
        );
    }

}
