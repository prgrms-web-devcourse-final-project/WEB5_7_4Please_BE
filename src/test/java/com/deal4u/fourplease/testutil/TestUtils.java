package com.deal4u.fourplease.testutil;

import com.deal4u.fourplease.domain.auction.dto.AuctionCreateRequest;
import com.deal4u.fourplease.domain.auction.dto.AuctionDetailResponse;
import com.deal4u.fourplease.domain.auction.dto.AuctionListResponse;
import com.deal4u.fourplease.domain.auction.dto.CategoryDto;
import com.deal4u.fourplease.domain.auction.dto.ProductCreateDto;
import com.deal4u.fourplease.domain.auction.dto.SellerSaleListResponse;
import com.deal4u.fourplease.domain.auction.entity.Address;
import com.deal4u.fourplease.domain.auction.entity.Auction;
import com.deal4u.fourplease.domain.auction.entity.AuctionDuration;
import com.deal4u.fourplease.domain.auction.entity.BidPeriod;
import com.deal4u.fourplease.domain.auction.entity.Category;
import com.deal4u.fourplease.domain.auction.entity.Product;
import com.deal4u.fourplease.domain.auction.entity.ProductImage;
import com.deal4u.fourplease.domain.auction.entity.Seller;
import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.domain.member.entity.Status;
import com.deal4u.fourplease.domain.wishlist.dto.WishlistResponse;
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
                .provider("provider")
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
                .seller(Seller.create(genMember()))
                .category(new Category(4L, "생활용품"))
                .phone("010-0000-0000")
                .build();
    }

    public static Auction genAuction() {
        return genAuctionCreateRequest().toEntity(genProduct());
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
                BigDecimal.valueOf(100000),
                null
        );
    }

    public static AuctionDetailResponse genAuctionDetailResponse() {
        return new AuctionDetailResponse(
                BigDecimal.valueOf(2000000),
                null,
                20,
                BigDecimal.valueOf(1000000),
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

    public static List<AuctionListResponse> genAuctionListResponseList() {
        return List.of(
                new AuctionListResponse(
                        1L,
                        "http://example.com/thumbnail1.jpg",
                        new CategoryDto(0L, "패션"),
                        "목도리",
                        BigDecimal.valueOf(200000),
                        BigDecimal.valueOf(250000),
                        5,
                        LocalDateTime.now().plusDays(3),
                        false
                ),
                new AuctionListResponse(
                        2L,
                        "http://example.com/thumbnail2.jpg",
                        new CategoryDto(2L, "스포츠"),
                        "축구공",
                        BigDecimal.valueOf(10000000),
                        null,
                        150,
                        LocalDateTime.now().plusDays(7),
                        true
                ),
                new AuctionListResponse(
                        3L,
                        "http://example.com/thumbnail3.jpg",
                        new CategoryDto(4L, "생활용품"),
                        "칫솔",
                        BigDecimal.valueOf(2000000),
                        null,
                        20,
                        LocalDateTime.now(),
                        false
                )
        );
    }

    public static PageResponse<AuctionListResponse> genAuctionListResponsePageResponse() {
        return PageResponse.<AuctionListResponse>builder()
                .content(genAuctionListResponseList())
                .totalElements(3L)
                .totalPages(1)
                .page(0)
                .size(20)
                .build();

    }

    public static List<Auction> genAuctionList() {
        return List.of(
                Auction.builder()
                        .auctionId(1L)
                        .product(
                                Product.builder()
                                        .name("목도리")
                                        .description("목에 둘러도 따뜻하지 않은 목도리")
                                        .thumbnailUrl("http://example.com/thumbnail1.jpg")
                                        .address(new Address(
                                                "서울시 서초구",
                                                "101동 102호",
                                                "999999"
                                        ))
                                        .seller(Seller.create(genMember()))
                                        .category(new Category(0L, "패션"))
                                        .phone("010-9999-9999")
                                        .build()
                        )
                        .startingPrice(BigDecimal.valueOf(100000))
                        .instantBidPrice(BigDecimal.valueOf(250000))
                        .duration(new AuctionDuration(LocalDateTime.now(),
                                LocalDateTime.now().plusDays(3)))
                        .build(),
                Auction.builder()
                        .auctionId(2L)
                        .product(
                                Product.builder()
                                        .name("축구공")
                                        .description("손흥민의 싸인이 있는 축구공")
                                        .thumbnailUrl("http://example.com/thumbnail2.jpg")
                                        .address(new Address(
                                                "서울시 광진구",
                                                "101동 102호",
                                                "333333"
                                        ))
                                        .seller(Seller.create(genMember()))
                                        .category(new Category(2L, "스포츠"))
                                        .phone("010-3333-3333")
                                        .build()
                        )
                        .startingPrice(BigDecimal.valueOf(5000000))
                        .instantBidPrice(null)
                        .duration(new AuctionDuration(LocalDateTime.now(),
                                LocalDateTime.now().plusDays(7)))
                        .build(),
                Auction.builder()
                        .auctionId(3L)
                        .product(
                                Product.builder()
                                        .name("칫솔")
                                        .description("한 번도 사용하지 않은 새 칫솔 입니다. 치약은 없습니다.")
                                        .thumbnailUrl("http://example.com/thumbnail3.jpg")
                                        .address(new Address(
                                                "서울시 강남구",
                                                "101동 102호",
                                                "000000"
                                        ))
                                        .seller(Seller.create(genMember()))
                                        .category(new Category(4L, "생활용품"))
                                        .phone("010-0000-0000")
                                        .build()
                        )
                        .startingPrice(BigDecimal.valueOf(100000))
                        .instantBidPrice(null)
                        .duration(new AuctionDuration(LocalDateTime.now(),
                                LocalDateTime.now().plusDays(1)))
                        .build()

        );
    }

    public static PageResponse<SellerSaleListResponse> genSellerSaleListResponsePageResponse() {
        List<SellerSaleListResponse> content = List.of(
                new SellerSaleListResponse(
                        1L,
                        "http://example.com/thumbnail1.jpg",
                        "목도리",
                        BigDecimal.valueOf(2000000),
                        BigDecimal.valueOf(1000000),
                        "목에 둘러도 따뜻하지 않은 목도리",
                        5,
                        "OPEN"
                ),
                new SellerSaleListResponse(
                        2L,
                        "http://example.com/thumbnail2.jpg",
                        "축구공",
                        BigDecimal.valueOf(10000000),
                        BigDecimal.valueOf(5000000),
                        "손흥민의 싸인이 있는 축구공",
                        20,
                        "OPEN"
                ),
                new SellerSaleListResponse(
                        3L,
                        "http://example.com/thumbnail3.jpg",
                        "칫솔",
                        BigDecimal.valueOf(2000000),
                        BigDecimal.valueOf(100000),
                        "한 번도 사용하지 않은 새 칫솔 입니다. 치약은 없습니다.",
                        20,
                        "CLOSE"
                )
        );

        return PageResponse.<SellerSaleListResponse>builder()
                .content(content)
                .totalElements(3L)
                .totalPages(1)
                .page(0)
                .size(20)
                .build();
    }

    public static List<Product> genProductList() {
        return List.of(
                Product.builder()
                        .productId(1L)
                        .name("목도리")
                        .description("목에 둘러도 따뜻하지 않은 목도리")
                        .thumbnailUrl("http://example.com/thumbnail1.jpg")
                        .address(new Address(
                                "서울시 서초구",
                                "101동 102호",
                                "999999"
                        ))
                        .seller(Seller.create(genMember()))
                        .category(new Category(0L, "패션"))
                        .phone("010-9999-9999")
                        .build(),
                Product.builder()
                        .productId(2L)
                        .name("축구공")
                        .description("손흥민의 싸인이 있는 축구공")
                        .thumbnailUrl("http://example.com/thumbnail2.jpg")
                        .address(new Address(
                                "서울시 광진구",
                                "101동 102호",
                                "333333"
                        ))
                        .seller(Seller.create(genMember()))
                        .category(new Category(2L, "스포츠"))
                        .phone("010-3333-3333")
                        .build(),
                Product.builder()
                        .productId(3L)
                        .name("칫솔")
                        .description("한 번도 사용하지 않은 새 칫솔 입니다. 치약은 없습니다.")
                        .thumbnailUrl("http://example.com/thumbnail3.jpg")
                        .address(new Address(
                                "서울시 강남구",
                                "101동 102호",
                                "000000"
                        ))
                        .seller(Seller.create(genMember()))
                        .category(new Category(4L, "생활용품"))
                        .phone("010-0000-0000")
                        .build()
        );
    }

    public static List<WishlistResponse> genWishlistResponseList() {
        return List.of(
                new WishlistResponse(
                        2L,
                        1L,
                        "http://example.com/thumbnail1.jpg",
                        "목도리",
                        BigDecimal.valueOf(2000000),
                        5,
                        LocalDateTime.now().minusDays(3)
                ),
                new WishlistResponse(
                        3L,
                        2L,
                        "http://example.com/thumbnail2.jpg",
                        "축구공",
                        BigDecimal.valueOf(10000000),
                        20,
                        LocalDateTime.now().minusDays(2)
                ),
                new WishlistResponse(
                        1L,
                        3L,
                        "http://example.com/thumbnail3.jpg",
                        "칫솔",
                        BigDecimal.valueOf(2000000),
                        20,
                        LocalDateTime.now().minusDays(1)
                )
        );
    }

    public static PageResponse<WishlistResponse> genWishlistResponsePageResponse() {
        return PageResponse.<WishlistResponse>builder()
                .content(genWishlistResponseList())
                .totalElements(3L)
                .totalPages(1)
                .page(0)
                .size(20)
                .build();
    }
}
