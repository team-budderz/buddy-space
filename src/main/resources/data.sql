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
    ('Alice', 'alice@example.com', 'password123', '1995-05-10', 'F', 'Seoul, Gangnam-gu', '010-1234-5678', NULL, 'LOCAL', NULL, 'USER', 1),
    ('Bob', 'bob@example.com', 'securepw', '1990-10-22', 'M', 'Busan, Haeundae-gu', '010-8765-4321', NULL, 'KAKAO', 'kakao123', 'USER', NULL);


-- 3. Group 데이터
INSERT INTO groups (
    name, description, access, type, interest, is_neighborhood_auth_required, leader_id, neighborhood_id)
VALUES
      ('Hiking Club', 'Group for weekend hikes', 'PUBLIC', 'ONLINE', 'STUDY', true, 1, 1),
      ('Book Lovers', 'Discussing books monthly', 'PRIVATE', 'OFFLINE', 'EXERCISE', false, 2, NULL);

-- 4. Post 데이터
INSERT INTO posts (
    id, group_id, user_id, content, reserve_at, is_notice)
VALUES
    (1, 1, 1, '내용 1번', null, false),
    (2, 1, 2, '내용 2번', null, false);