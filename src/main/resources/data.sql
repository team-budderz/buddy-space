-- 1. Neighborhood 데이터
INSERT INTO neighborhoods (city_name, district_name, ward_name, lat, lng, is_verified)
VALUES
    ('Seoul', 'Gangnam-gu', 'Yeoksam-dong', 37.501274, 127.039585, true),
    ('Busan', 'Haeundae-gu', 'U-dong', 35.1631, 129.1635, true);

-- 2. User 데이터
INSERT INTO users (name, email, password, birth_date, gender, address, phone, image_url, provider, provider_id, role, neighborhood_id)
VALUES
    ('Alice', 'alice@example.com', 'Password123@', '1995-05-10', 'F', 'Seoul, Gangnam-gu', '010-1234-5678', NULL, 'LOCAL', NULL, 'USER', 1),
    ('Bob', 'bob@example.com', 'securepw', '1990-10-22', 'M', 'Busan, Haeundae-gu', '010-8765-4321', NULL, 'LOCAL', NULL, 'USER', 2);

-- 3. Group 데이터
INSERT INTO groups (
    name, description, access, type, interest, is_neighborhood_auth_required, leader_id, neighborhood_id
) VALUES
      ('Hiking Club', 'Group for weekend hikes', 'PUBLIC', 'ONLINE', 'STUDY', true, 1, 1),
      ('Book Lovers', 'Discussing books monthly', 'PRIVATE', 'OFFLINE', 'EXERCISE', false, 2, 2);

-- 4. ChatRoom 데이터
INSERT INTO chat_room (
    id, group_id, created_at, modified_at, created_by, chat_room_type, name, description
) VALUES
    (1, 1, now(), now(), 1, 'GROUP', '하이킹 그룹 채팅방', '산 이야기 나누는 방');

-- 5. ChatMessage 데이터
INSERT INTO chat_message (
    chat_room_id, sender_id, content, message_type, sent_at
) VALUES
      (1, 1, '안녕하세요! 첫 번째 채팅입니다.', 'TEXT', now()),
      (1, 2, '다음 모임 일정 잡아야죠!', 'TEXT', now());

-- 6. Post 데이터
INSERT INTO posts (
    group_id, user_id, content, reserve_at, is_notice
) VALUES
      (1, 1, '내용 1번', null, false),
      (1, 2, '내용 2번', null, false);

-- 7. Comments 데이터
INSERT INTO comments (
    post_id, user_id, content, parent_id
) VALUES
      (1, 1, '첫 번째 댓글입니다.', NULL),
      (1, 2, '첫 번째 댓글에 대한 대댓글입니다.', 1),
      (2, 1, '두 번째 게시글의 댓글입니다.', NULL),
      (2, 2, '두 번째 게시글 댓글에 대한 대댓글입니다.', 3);
