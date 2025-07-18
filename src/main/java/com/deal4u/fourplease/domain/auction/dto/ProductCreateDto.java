package com.deal4u.fourplease.domain.auction.dto;


import com.deal4u.fourplease.domain.auction.entity.Address;
import com.deal4u.fourplease.domain.auction.entity.Category;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProductCreateDto(

        Member member,

        String name,
        String description,
        String thumbnailUrl,
        List<String> imageUrls,
        Long categoryId,

        String address,
        String addressDetail,
        String zipCode,
        String phone

) {

    public Product toEntity(Category category) {
        Address address = new Address(this.address, addressDetail, zipCode);
        return Product.builder()
                .name(name)
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .address(address)
                .seller(Seller.create(member))
                .category(category)
                .phone(phone)
                .deleted(false)
                .build();
    }

}
