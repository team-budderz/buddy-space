package team.budderz.buddyspace.api.group.request;

import jakarta.validation.constraints.Size;
import team.budderz.buddyspace.infra.database.group.entity.GroupAccess;
import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;

/**
 * 모임 수정 요청 DTO
 */
public record UpdateGroupRequest(

        /**
         * 모임 이름
         */
        @Size(max = 20, message = "모임 이름은 20자를 초과할 수 없습니다.")
        String name,

        /**
         * 모임 커버 이미지 주소
         */
        String coverImageUrl,

        /**
         * 모임 소개글
         */
        @Size(max = 200, message = "모임 소개는 200자를 초과할 수 없습니다.")
        String description,

        /**
         * 모임 공개 여부 (GroupAccess Enum)
         * - PUBLIC, PRIVATE
         */
        GroupAccess access,

        /**
         * 모임 유형 (GroupType Enum)
         * - ONLINE, OFFLINE, HYBRID
         */
        GroupType type,

        /**
         * 모임 관심사 (GroupInterest Enum)
         * - HOBBY, FAMILY, SCHOOL, BUSINESS, EXERCISE, GAME, STUDY, FAN, OTHER
         */
        GroupInterest interest
) {}