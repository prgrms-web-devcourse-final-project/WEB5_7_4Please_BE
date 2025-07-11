-- 판매자 정보
INSERT INTO member (email, nick_name, role, status, created_at, updated_at)
VALUES ('seller@example.com', '판매자', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 입찰자A 정보
INSERT INTO member (email, nick_name, role, status, created_at, updated_at)
VALUES ( 'bidderA@example.com', '입찰자A', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 입찰자B 정보
INSERT INTO member (email, nick_name, role, status, created_at, updated_at)
VALUES ('bidderB@example.com', '입찰자B', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 카테고리 정보
INSERT INTO product_categories (name)
VALUES ('전자기기');

-- 상품 정보
INSERT INTO product (name, description, thumbnail_url, address, detail_address, zip_code,
                     seller_member_id, category_category_id, phone,
                     created_at, updated_at)
VALUES ('최신형 노트북', '한 번도 사용하지 않은 최신형 노트북입니다. 성능이 매우 뛰어납니다.',
        'https://example.com/images/laptop.jpg', '경기도 평택시 안산로 25번길 21', '100동 102호', '12345', 1L, 1, '010-1234-5678',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 경메 정보
INSERT INTO auction(product_product_id, starting_price, instant_bid_price, start_time,
                    end_time, status, deleted, created_at, updated_at)
VALUES ( 1L, 100000, 800000, CURRENT_TIMESTAMP, TIMESTAMPADD(DAY, 7, CURRENT_TIMESTAMP), 'OPEN',
        false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);