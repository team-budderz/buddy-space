package team.budderz.buddyspace.domain.chat.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.request.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.CreateChatRoomResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.profiles.active=test")
@Transactional // 테스트 끝나면 롤백
class ChatRoomServiceTest {

    @Autowired
    ChatRoomService chatRoomService;

    @Test
    void 채팅방_생성_테스트() {
        // given
        Long groupId = 1L; // TODO: 테스트용 groupId (데이터 준비 필요)
        Long userId = 1L;  // TODO: 테스트용 userId (데이터 준비 필요)

        CreateChatRoomRequest request = CreateChatRoomRequest.builder()
                .name("테스트 채팅방")
                .description("테스트용입니다.")
                .chatRoomType("GROUP")
                .participantIds(List.of(1L, 2L, 3L)) // TODO: 유효한 유저 ID 필요
                .build();

        // when
        CreateChatRoomResponse response = chatRoomService.createChatRoom(groupId, userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("테스트 채팅방");

        System.out.println("생성된 ChatRoom ID: " + response.getRoomId());
    }
}
