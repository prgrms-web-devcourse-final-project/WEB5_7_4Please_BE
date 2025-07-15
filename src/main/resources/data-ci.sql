-- 이 파일은 CI 환경의 H2 데이터베이스에서만 사용됩니다.

-- 기존 데이터 삭제
DELETE
FROM bid;
DELETE
FROM auctions;
DELETE
FROM products;
DELETE
FROM product_categories;
DELETE
FROM member;

-- H2 DB의 경우 ID 시퀀스 초기화
ALTER TABLE member
    ALTER COLUMN id RESTART WITH 1;
ALTER TABLE product_categories
    ALTER COLUMN category_id RESTART WITH 1;
ALTER TABLE products
    ALTER COLUMN product_id RESTART WITH 1;
ALTER TABLE auctions
    ALTER COLUMN auction_id RESTART WITH 1;
ALTER TABLE bid
    ALTER COLUMN bid_id RESTART WITH 1;

-- =================================================================
-- 1. 회원 정보 (Member)
-- =================================================================

-- 판매자 정보
-- role, status와 같은 예약어는 백틱(`) 또는 큰따옴표(")로 감싸주는 것이 안전합니다.
INSERT INTO member (email, nick_name, `role`, `status`, created_at, updated_at)
VALUES ('seller@example.com', '판매자', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 입찰자 정보 (총 20명, ID: 2 ~ 21)
INSERT INTO member (email, nick_name, role, status, created_at, updated_at)
VALUES ('bidderA@example.com', '입찰자A', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 2
       ('bidderB@example.com', '입찰자B', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 3
       ('bidderC@example.com', '입찰자C', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 4
       ('bidderD@example.com', '입찰자D', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 5
       ('bidderE@example.com', '입찰자E', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 6
       ('bidderF@example.com', '입찰자F', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 7
       ('bidderG@example.com', '입찰자G', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 8
       ('bidderH@example.com', '입찰자H', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 9
       ('bidderI@example.com', '입찰자I', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 10
       ('bidderJ@example.com', '입찰자J', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 11
       ('bidderK@example.com', '입찰자K', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 12
       ('bidderL@example.com', '입찰자L', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 13
       ('bidderM@example.com', '입찰자M', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 14
       ('bidderN@example.com', '입찰자N', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 15
       ('bidderO@example.com', '입찰자O', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 16
       ('bidderP@example.com', '입찰자P', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 17
       ('bidderQ@example.com', '입찰자Q', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 18
       ('bidderR@example.com', '입찰자R', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 19
       ('bidderS@example.com', '입찰자S', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 20
       ('bidder1@example.com', '입찰자1', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP), -- ID: 21
       ('bidder2@example.com', '입찰자2', 'USER', 'ACTIVE', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP) -- ID: 22
;

-- =================================================================
-- 2. 상품 및 경매 정보 (Product & Auction)
-- =================================================================

-- 카테고리 정보
INSERT INTO product_categories (category_id, name)
VALUES (0, '패션');
INSERT INTO product_categories (category_id, name)
VALUES (1, '전자제품');
INSERT INTO product_categories (category_id, name)
VALUES (2, '스포츠');
INSERT INTO product_categories (category_id, name)
VALUES (3, '가구');
INSERT INTO product_categories (category_id, name)
VALUES (4, '생활용품');
INSERT INTO product_categories (category_id, name)
VALUES (5, '기타');

-- 상품 정보
INSERT INTO products (name, description, thumbnail_url, address, detail_address, zip_code,
                      seller_member_id, category_category_id, phone, deleted,
                      created_at, updated_at)
VALUES ('최신형 노트북', '한 번도 사용하지 않은 최신형 노트북입니다. 성능이 매우 뛰어납니다.',
        'https://example.com/images/laptop.jpg', '경기도 평택시 안산로 25번길 21', '100동 102호', '12345', 1, 1,
        '010-1234-5678', false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('최신형 IPhone 16', '미개봉 IPhone 16입니다. 256gb, 컬러는 화이트입니다. 쿨거래시 네고 가능합니다.',
        'https://example.com/images/iPhone16.jpg', '경기도 오산시 안산로 25번길 21', '100동 1004호', '12333', 2,
        1,
        '010-2222-1111', false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 경메 정보
-- TIMESTAMPADD를 H2 호환 함수인 DATEADD로 변경하고, status 컬럼을 감쌌습니다.
INSERT INTO auctions (product_product_id, starting_price, instant_bid_price, start_time,
                      end_time, `status`, deleted, created_at, updated_at)
VALUES (1, 100000, 800000, CURRENT_TIMESTAMP, DATEADD('DAY', 7, CURRENT_TIMESTAMP), 'OPEN',
        false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 1200000, 1500000, CURRENT_TIMESTAMP, DATEADD('DAY', 7, CURRENT_TIMESTAMP), 'OPEN',
        false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =================================================================
-- 3. 입찰 정보 (Bid)
-- =================================================================
-- 총 20(19개 일반 / 1개 삭제)개의 입찰 정보. 정렬(가격 내림차순, 시간 오름차순) 테스트를 위해 가격과 시간을 다르게 설정
-- Bid 엔티티의 @Embedded Bidder 필드에 맞춰 bidder_member_id, bidder_nick_name 컬럼에 값을 삽입합니다.
-- 1개는 삭제된 입찰 정보이기 때문에 조회되지 않습니다.
INSERT INTO bid (auction_id, bidder_member_id, price, bid_time,
                 is_successful_bidder, deleted, created_at, updated_at)
VALUES (1, 2, 200000, TIMESTAMPADD(SECOND, -20, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 3, 199000, TIMESTAMPADD(SECOND, -19, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 4, 198000, TIMESTAMPADD(SECOND, -18, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 5, 197000, TIMESTAMPADD(SECOND, -17, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 6, 196000, TIMESTAMPADD(SECOND, -16, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 7, 195000, TIMESTAMPADD(SECOND, -15, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 8, 194000, TIMESTAMPADD(SECOND, -14, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 9, 193000, TIMESTAMPADD(SECOND, -13, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 10, 192000, TIMESTAMPADD(SECOND, -12, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 11, 191000, TIMESTAMPADD(SECOND, -11, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 12, 190000, TIMESTAMPADD(SECOND, -10, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 13, 189000, TIMESTAMPADD(SECOND, -9, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 14, 188000, TIMESTAMPADD(SECOND, -8, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 15, 187000, TIMESTAMPADD(SECOND, -7, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 16, 186000, TIMESTAMPADD(SECOND, -6, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 17, 185000, TIMESTAMPADD(SECOND, -5, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 18, 184000, TIMESTAMPADD(SECOND, -4, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 19, 183000, TIMESTAMPADD(SECOND, -3, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 20, 182000, TIMESTAMPADD(SECOND, -2, CURRENT_TIMESTAMP), false, true,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 20, 200000, TIMESTAMPADD(SECOND, -1, CURRENT_TIMESTAMP), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
