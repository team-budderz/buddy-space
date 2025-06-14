package team.budderz.buddyspace.domain.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.request.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.CreateChatRoomResponse;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoomType;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.profiles.active=test")
@Transactional // 테스트 끝나면 롤백
class ChatRoomServiceTest {

    @Autowired
    ChatRoomCommandService chatRoomService;

    @Autowired
    MembershipRepository membershipRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        User user = userRepository.findById(1L).orElseThrow();
        Group group = groupRepository.findById(1L).orElseThrow();

        Membership membership = Membership.fromInvite(user, group);
        membershipRepository.save(membership);
    }


    // @Test
    // void 채팅방_생성_테스트() {
    //     // given
    //     Long groupId = 1L; // TODO: 테스트용 groupId (데이터 준비 필요)
    //     Long userId = 1L;  // TODO: 테스트용 userId (데이터 준비 필요)
    //
    //     CreateChatRoomRequest request = new CreateChatRoomRequest(
    //             "테스트 채팅방",
    //             "테스트용입니다.",
    //             ChatRoomType.GROUP,
    //             List.of(1L, 2L, 3L)
    //     );
    //
    //     // when
    //     CreateChatRoomResponse response = chatRoomService.createChatRoom(groupId, userId, request);
    //
    //     // then
    //     assertThat(response).isNotNull();
    //     assertThat(response.name()).isEqualTo("테스트 채팅방");
    //
    //     System.out.println("생성된 ChatRoom ID: " + response.roomId());
    // }
}
