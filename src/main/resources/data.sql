-- 이 파일은 MySQL 데이터베이스에서 사용됩니다.

-- 기존 데이터 삭제 (외래키 제약조건을 고려한 순서)
DELETE FROM bid;
DELETE FROM auctions;
DELETE FROM products;
DELETE FROM product_categories;
DELETE FROM member;
DELETE FROM settlement;
DELETE FROM orders;

-- =================================================================
-- 1. 회원 정보 (Member)
-- =================================================================

-- 판매자 정보
INSERT INTO member (email, nick_name, `role`, `status`, provider, created_at, updated_at)
VALUES ('seller@example.com', '판매자', 'USER', 'ACTIVE', 'naver', NOW(), NOW());

-- 입찰자 정보 (총 21명, ID: 2 ~ 22)
INSERT INTO member (email, nick_name, `role`, `status`, provider, created_at, updated_at)
VALUES ('bidderA@example.com', '입찰자A', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 2
       ('bidderB@example.com', '입찰자B', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 3
       ('bidderC@example.com', '입찰자C', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 4
       ('bidderD@example.com', '입찰자D', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 5
       ('bidderE@example.com', '입찰자E', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 6
       ('bidderF@example.com', '입찰자F', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 7
       ('bidderG@example.com', '입찰자G', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 8
       ('bidderH@example.com', '입찰자H', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 9
       ('bidderI@example.com', '입찰자I', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 10
       ('bidderJ@example.com', '입찰자J', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 11
       ('bidderK@example.com', '입찰자K', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 12
       ('bidderL@example.com', '입찰자L', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 13
       ('bidderM@example.com', '입찰자M', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 14
       ('bidderN@example.com', '입찰자N', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 15
       ('bidderO@example.com', '입찰자O', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 16
       ('bidderP@example.com', '입찰자P', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 17
       ('bidderQ@example.com', '입찰자Q', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 18
       ('bidderR@example.com', '입찰자R', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 19
       ('bidderS@example.com', '입찰자S', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 20
       ('bidder1@example.com', '입찰자1', 'USER', 'ACTIVE', 'naver', NOW(), NOW()), -- ID: 21
       ('bidder2@example.com', '입찰자2', 'USER', 'ACTIVE', 'naver', NOW(), NOW()); -- ID: 22

-- =================================================================
-- 2. 상품 및 경매 정보 (Product & Auction)
-- =================================================================

-- 카테고리 정보
INSERT INTO product_categories (category_id, name)
VALUES (0, ''),
       (1, '패션'),
       (2, '전자제품'),
       (3, '스포츠'),
       (4, '가구'),
       (5, '생활용품'),
       (6, '기타');

-- 상품 정보
INSERT INTO products (name, description, thumbnail_url, address, detail_address, zip_code,
                      seller_member_id, category_category_id, phone, deleted,
                      created_at, updated_at)
VALUES ('최신형 노트북', '한 번도 사용하지 않은 최신형 노트북입니다. 성능이 매우 뛰어납니다.',
        'https://example.com/images/laptop.jpg', '경기도 평택시 안산로 25번길 21', '100동 102호', '12345', 1, 1,
        '010-1234-5678', false, NOW(), NOW()),
       ('최신형 IPhone 16', '미개봉 IPhone 16입니다. 256gb, 컬러는 화이트입니다. 쿨거래시 네고 가능합니다.',
        'https://example.com/images/iPhone16.jpg', '경기도 오산시 안산로 25번길 21', '100동 1004호', '12333', 2,
        1, '010-2222-1111', false, NOW(), NOW()),
       ('삼성 갤럭시 Z 플립6', '갤럭시 Z 플립6 새제품입니다. 퍼플 컬러이며, 박스 미개봉입니다. 직거래 및 택배 모두 가능입니다.',
        'https://example.com/images/galaxyZFlip6.jpg', '서울특별시 마포구 월드컵북로 396', '202호', '04567', 3,
        1, '010-3333-4444', false, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
       ('삭제를 위한 품목', '삭제 테스트를 위해서 추가한 데이터',
        'https://example.com/images/delete.jpg', '경기도 평택시 안산로 25번길 21', '100동 102호', '12345', 1, 1,
        '010-1234-5678', false, NOW(), NOW()),
       ('애플 아이폰 15 프로 맥스',
        '아이폰 15 프로 맥스 미드나이트 블랙 색상입니다. 박스 개봉만 했으며, 미사용 상태입니다. 직거래만 가능합니다.',
        'https://example.com/images/iphone15promax.jpg',
        '부산광역시 해운대구 센텀서로 30',
        '1501호',
        '61200',
        1,
        0,
        '010-9999-8888',
        false,
        DATE_SUB(NOW(), INTERVAL 4 DAY),
        DATE_SUB(NOW(), INTERVAL 4 DAY)
       ),
       ('애플 아이폰 1260 프로 맥스',
        '아이폰 1260 프로 맥스 미드나이트 블랙 색상입니다. 박스 개봉만 했으며, 미사용 상태입니다. 직거래만 가능합니다.',
        'https://example.com/images/iphone15promax.jpg',
        '부산광역시 해운대구 센텀서로 30',
        '1501호',
        '61200',
        1,
        0,
        '010-9999-8888',
        false,
        DATE_SUB(NOW(), INTERVAL 4 DAY),
        DATE_SUB(NOW(), INTERVAL 4 DAY)
       );

-- 경매 정보
INSERT INTO auctions (product_product_id, starting_price, instant_bid_price, start_time,
                      end_time, `status`, deleted, created_at, updated_at)
VALUES (1, 100000, 800000, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'OPEN',
        false, NOW(), NOW()),
       (2, 1200000, 1500000, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'OPEN',
        false, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY)),
       (3, 1000, null, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'CLOSE',
        false, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY)),
       (4, 10000, 800000, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'OPEN',
        false, NOW(), NOW()),
       (5, 1000, null, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'CLOSE',
        false, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY)),
       (6, 1000, 50000, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'OPEN',
        false, NOW(), NOW());

-- =================================================================
-- 3. 입찰 정보 (Bid)
-- =================================================================
-- 총 25개의 입찰 정보. 정렬(가격 내림차순, 시간 오름차순) 테스트를 위해 가격과 시간을 다르게 설정
-- 1개는 삭제된 입찰 정보이기 때문에 조회되지 않습니다.
INSERT INTO bid (auction_id, bidder_member_id, price, bid_time,
                 is_successful_bidder, deleted, created_at, updated_at)
VALUES (1, 2, 200000, DATE_SUB(NOW(), INTERVAL 20 SECOND), false, false, NOW(), NOW()),
       (1, 3, 199000, DATE_SUB(NOW(), INTERVAL 19 SECOND), false, false, NOW(), NOW()),
       (1, 4, 198000, DATE_SUB(NOW(), INTERVAL 18 SECOND), false, false, NOW(), NOW()),
       (1, 5, 197000, DATE_SUB(NOW(), INTERVAL 17 SECOND), false, false, NOW(), NOW()),
       (1, 6, 196000, DATE_SUB(NOW(), INTERVAL 16 SECOND), false, false, NOW(), NOW()),
       (1, 7, 195000, DATE_SUB(NOW(), INTERVAL 15 SECOND), false, false, NOW(), NOW()),
       (1, 8, 194000, DATE_SUB(NOW(), INTERVAL 14 SECOND), false, false, NOW(), NOW()),
       (1, 9, 193000, DATE_SUB(NOW(), INTERVAL 13 SECOND), false, false, NOW(), NOW()),
       (1, 10, 192000, DATE_SUB(NOW(), INTERVAL 12 SECOND), false, false, NOW(), NOW()),
       (1, 11, 191000, DATE_SUB(NOW(), INTERVAL 11 SECOND), false, false, NOW(), NOW()),
       (1, 12, 190000, DATE_SUB(NOW(), INTERVAL 10 SECOND), false, false, NOW(), NOW()),
       (1, 13, 189000, DATE_SUB(NOW(), INTERVAL 9 SECOND), false, false, NOW(), NOW()),
       (1, 14, 188000, DATE_SUB(NOW(), INTERVAL 8 SECOND), false, false, NOW(), NOW()),
       (1, 15, 187000, DATE_SUB(NOW(), INTERVAL 7 SECOND), false, false, NOW(), NOW()),
       (1, 16, 186000, DATE_SUB(NOW(), INTERVAL 6 SECOND), false, false, NOW(), NOW()),
       (1, 17, 185000, DATE_SUB(NOW(), INTERVAL 5 SECOND), false, false, NOW(), NOW()),
       (1, 18, 184000, DATE_SUB(NOW(), INTERVAL 4 SECOND), false, false, NOW(), NOW()),
       (1, 19, 183000, DATE_SUB(NOW(), INTERVAL 3 SECOND), false, false, NOW(), NOW()),
       (1, 20, 182000, DATE_SUB(NOW(), INTERVAL 2 SECOND), false, true, NOW(), NOW()),
       (1, 20, 200000, DATE_SUB(NOW(), INTERVAL 1 SECOND), false, false, NOW(), NOW()),
       (3, 2, 1000, DATE_SUB(NOW(), INTERVAL 1 SECOND), false, false, NOW(), NOW()),
       (3, 3, 2000, DATE_SUB(NOW(), INTERVAL 1 SECOND), true, false, NOW(), NOW()),
       (4, 21, 20000, DATE_SUB(NOW(), INTERVAL 1 SECOND), true, false, NOW(), NOW()),
       (5, 2, 1000, DATE_SUB(NOW(), INTERVAL 1 SECOND), false, false, NOW(), NOW()),
       (5, 3, 2000, DATE_SUB(NOW(), INTERVAL 1 SECOND), true, false, NOW(), NOW());

-- =================================================================
-- 4. 정산 정보 (Settlement)
-- =================================================================
INSERT INTO settlement (
    auction_id,
    bidder_member_id,
    status,
    payment_deadline,
    rejected_reason,
    paid_at,
    created_at,
    updated_at
) VALUES (
             3,                     -- auction_id (참조하는 경매 ID)
             22,                    -- bidder_member_id
             'PENDING',             -- status (ENUM)
             DATE_ADD(NOW(), INTERVAL 3 DAY),  -- 결제 마감일: 3일 뒤
             NULL,                  -- 거절 사유 없음
             NULL,                  -- 결제 완료 시간 없음
             NOW(),                 -- 생성일
             NOW()                  -- 수정일
         ),
         (
             5,                     -- auction_id (참조하는 경매 ID)
             3,                     -- bidder_member_id
             'PENDING',             -- status (ENUM)
             DATE_ADD(NOW(), INTERVAL 3 DAY),  -- 결제 마감일: 3일 뒤
             NULL,                  -- 거절 사유 없음
             NULL,                  -- 결제 완료 시간 없음
             NOW(),                 -- 생성일
             NOW()                  -- 수정일
         );

-- =================================================================
-- 5. 주문 정보 (Orders)
-- =================================================================
INSERT INTO orders (
    price,
    auction_auction_id,
    created_at,
    member_member_id,
    updated_at,
    address,
    address_detail,
    content,
    order_id,
    phone,
    receiver,
    zip_code,
    order_status,
    order_type
) VALUES (
             2000.00,
             5,
             NOW(),
             22,
             NOW(),
             '경기도 성남시 분당구 판교로 235',
             '우림W시티 706호',
             '빠른 배송 부탁드립니다.',
             'ORD-20250726-0002',
             '010-9876-5432',
             '이영희',
             '13529',
             'PENDING',
             'AWARD'
         );