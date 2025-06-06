-- User 성별, 공급자, 역할 Enum 가정 (스키마에 따라 적절히 바꾸세요)
-- UserGender: 'M', 'F'
-- UserProvider: 'LOCAL', 'KAKAO'
-- UserRole: 'USER', 'ADMIN'
-- GroupType: 'ONLINE', 'OFFLINE', 'HYBRID'
-- InterestType: 'STUDY', 'EXERCISE', 'WALK', 'READING'

-- 1. Neighborhood 데이터
INSERT INTO neighborhoods (city_name, district_name, ward_name, lat, lng, is_verified)
VALUES
    ('Seoul', 'Gangnam-gu', 'Yeoksam-dong', 37.501274, 127.039585, true),
    ('Busan', 'Haeundae-gu', 'U-dong', 35.1631, 129.1635, true);

-- 2. User 데이터
INSERT INTO users (name, email, password, birth_date, gender, address, phone, image_url, provider, provider_id, role, neighborhood_id)
VALUES
    ('Alice', 'alice@example.com', 'Password123@', '1995-05-10', 'F', 'Seoul, Gangnam-gu', '010-1234-5678', NULL, 'LOCAL', NULL, 'USER', 1),
    ('Bob', 'bob@example.com', 'securepw', '1990-10-22', 'M', 'Busan, Haeundae-gu', '010-8765-4321', NULL, 'LOCAL', 'NULL', 'USER', NULL);


-- 3. Group 데이터
INSERT INTO groups (
    name, description, access, type, interest, is_neighborhood_auth_required, leader_id, neighborhood_id)
VALUES
      ('Hiking Club', 'Group for weekend hikes', 'PUBLIC', 'ONLINE', 'STUDY', true, 1, 1),
      ('Book Lovers', 'Discussing books monthly', 'PRIVATE', 'OFFLINE', 'EXERCISE', false, 2, NULL);

-- 4. Post 데이터
INSERT INTO posts (
    group_id, user_id, content, reserve_at, is_notice)
VALUES
    (1, 1, '내용 1번', null, false),
    (1, 2, '내용 2번', null, false);

-- 5. Comments 데이터
INSERT INTO comments (
    post_id, user_id, content, parent_id
) VALUES
      (1, 1, '첫 번째 댓글입니다.', NULL),
      (1, 2, '첫 번째 댓글에 대한 대댓글입니다.', 1),
      (2, 1, '두 번째 게시글의 댓글입니다.', NULL),
      (2, 2, '두 번째 게시글 댓글에 대한 대댓글입니다.', 3);
