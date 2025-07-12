package com.deal4u.fourplease.domain.auction.dto;


import com.deal4u.fourplease.domain.auction.entity.Address;
import com.deal4u.fourplease.domain.auction.entity.Category;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.member.entity.TempMember;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProductCreateDto(

        @NotNull(message = "판매자 정보를 입력해 주세요.")
        TempMember member,

        @NotBlank(message = "상품 이름을 입력해 주세요.")
        @Size(min = 1, max = 20, message = "상품 이름은 1자 이상 20자 이하로 입력해주세요.")
        String name,

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
        String phone

) {

    public Product toEntity(Category category) {
        Address address = new Address(this.address, addressDetail, zipCode);
        return Product.builder()
                .name(name)
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .address(address)
                .seller(new Seller(member))
                .category(category)
                .phone(phone)
                .deleted(false)
                .build();
    }

}
