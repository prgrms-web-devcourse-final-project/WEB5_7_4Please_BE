-- MySQL 호환 data-prod.sql 파일

-- 기존 데이터 삭제
DELETE FROM bid;
DELETE FROM auctions;
DELETE FROM products;
DELETE FROM product_categories;
DELETE FROM member;

-- =================================================================
-- 1. 회원 정보 (Member)
-- =================================================================

-- 판매자 정보
-- role, status와 같은 예약어는 백틱(`) 또는 큰따옴표(")로 감싸주는 것이 안전합니다.
INSERT INTO member (email, nick_name, `role`, `status`, provider, created_at, updated_at)
VALUES ('seller@example.com', '판매자', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 입찰자 정보 (총 20명, ID: 2 ~ 21)
INSERT INTO member (email, nick_name, role, status, provider, created_at, updated_at)
VALUES ('bidderA@example.com', '입찰자A', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 2
       ('bidderB@example.com', '입찰자B', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 3
       ('bidderC@example.com', '입찰자C', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 4
       ('bidderD@example.com', '입찰자D', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 5
       ('bidderE@example.com', '입찰자E', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 6
       ('bidderF@example.com', '입찰자F', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 7
       ('bidderG@example.com', '입찰자G', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 8
       ('bidderH@example.com', '입찰자H', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 9
       ('bidderI@example.com', '입찰자I', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 10
       ('bidderJ@example.com', '입찰자J', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 11
       ('bidderK@example.com', '입찰자K', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 12
       ('bidderL@example.com', '입찰자L', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 13
       ('bidderM@example.com', '입찰자M', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 14
       ('bidderN@example.com', '입찰자N', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 15
       ('bidderO@example.com', '입찰자O', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 16
       ('bidderP@example.com', '입찰자P', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 17
       ('bidderQ@example.com', '입찰자Q', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 18
       ('bidderR@example.com', '입찰자R', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 19
       ('bidderS@example.com', '입찰자S', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 20
       ('bidder1@example.com', '입찰자1', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- ID: 21
       ('bidder2@example.com', '입찰자2', 'USER', 'ACTIVE', 'naver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); -- ID: 22

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
        'https://example.com/images/laptop.jpg', '경기도 평택시 안산로 25번길 21', '100동 102호', '12345', 1, 2,
        '010-1234-5678', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('최신형 IPhone 16', '미개봉 IPhone 16입니다. 256gb, 컬러는 화이트입니다. 쿨거래시 네고 가능합니다.',
        'https://example.com/images/iPhone16.jpg', '경기도 오산시 안산로 25번길 21', '100동 1004호', '12333', 2, 2,
        '010-2222-1111', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('삼성 갤럭시 Z 플립6', '갤럭시 Z 플립6 새제품입니다. 퍼플 컬러이며, 박스 미개봉입니다. 직거래 및 택배 모두 가능합니다.',
        'https://example.com/images/galaxyZFlip6.jpg', '서울특별시 마포구 월드컵북로 396', '202호', '04567', 3, 2,
        '010-3333-4444', false, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY)),
       ('삭제를 위한 품목', '삭제 테스트를 위해서 추가한 데이터',
        'https://example.com/images/delete.jpg', '경기도 평택시 안산로 25번길 21', '100동 102호', '12345', 1, 2,
        '010-1234-5678', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('애플 아이폰 15 프로 맥스',
        '아이폰 15 프로 맥스 미드나이트 블랙 색상입니다. 박스 개봉만 했으며, 미사용 상태입니다. 직거래만 가능합니다.',
        'https://example.com/images/iphone15promax.jpg', '부산광역시 해운대구 센텀서로 30', '1501호', '61200', 1, 1,
        '010-9999-8888', false, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY)),
       ('애플 아이폰 1260 프로 맥스',
        '아이폰 1260 프로 맥스 미드나이트 블랙 색상입니다. 박스 개봉만 했으며, 미사용 상태입니다. 직거래만 가능합니다.',
        'https://example.com/images/iphone15promax.jpg', '부산광역시 해운대구 센텀서로 30', '1501호', '61200', 1, 1,
        '010-9999-8888', false, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY));

-- 경매 정보
-- DATEADD를 DATE_ADD로 변경
INSERT INTO auctions (product_product_id, starting_price, instant_bid_price, start_time,
                      end_time, `status`, deleted, created_at, updated_at)
VALUES (1, 100000, 800000, CURRENT_TIMESTAMP, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY), 'OPEN',
        false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 1200000, 1500000, CURRENT_TIMESTAMP, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY), 'OPEN',
        false, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY)),
       (3, 1000, null, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'CLOSE',
        false, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY)),
       (4, 10000, 800000, CURRENT_TIMESTAMP, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY), 'OPEN',
        false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 1000, null, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'CLOSE',
        false, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY)),
       (6, 1000, 50000, CURRENT_TIMESTAMP, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY), 'OPEN',
        false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =================================================================
-- 3. 입찰 정보 (Bid)
-- =================================================================
-- TIMESTAMPADD를 DATE_SUB로 변경 (초 단위 계산)
INSERT INTO bid (auction_id, bidder_member_id, price, bid_time,
                 is_successful_bidder, deleted, created_at, updated_at)
VALUES (1, 2, 200000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 3, 199000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 19 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 4, 198000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 18 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 5, 197000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 6, 196000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 16 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 7, 195000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 15 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 8, 194000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 14 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 9, 193000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 13 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 10, 192000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 12 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 11, 191000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 11 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 12, 190000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 13, 189000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 9 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 14, 188000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 8 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 15, 187000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 16, 186000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 17, 185000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 18, 184000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 19, 183000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 20, 182000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 SECOND), false, true,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 20, 200000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 2, 1000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 3, 2000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 SECOND), true, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 21, 20000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 SECOND), true, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 2, 1000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 SECOND), false, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 3, 2000, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 SECOND), true, false,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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
             22,                    -- bidder_id
             'PENDING',             -- status (ENUM)
             DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 3 DAY),  -- 결제 마감일: 3일 뒤
             NULL,                  -- 거절 사유 없음
             NULL,                  -- 결제 완료 시간 없음
             CURRENT_TIMESTAMP,     -- 생성일
             CURRENT_TIMESTAMP      -- 수정일
         ),
         (
             5,                     -- auction_id (참조하는 경매 ID)
             3,                     -- bidder_id
             'PENDING',             -- status (ENUM)
             DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 3 DAY),  -- 결제 마감일: 3일 뒤
             NULL,                  -- 거절 사유 없음
             NULL,                  -- 결제 완료 시간 없음
             CURRENT_TIMESTAMP,     -- 생성일
             CURRENT_TIMESTAMP      -- 수정일
         );

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
             CURRENT_TIMESTAMP,
             22,
             CURRENT_TIMESTAMP,
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
