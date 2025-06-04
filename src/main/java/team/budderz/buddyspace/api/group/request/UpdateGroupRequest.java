package team.budderz.buddyspace.api.group.request;

import jakarta.validation.constraints.Size;
import team.budderz.buddyspace.infra.database.group.entity.GroupAccess;
import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;

/**
 * 모임 생성 요청 DTO
 */
public record UpdateGroupRequest(

        /**
         * 모임 이름 (필수, 최대 20자)
         */
        @Size(max = 20, message = "모임 이름은 20자를 초과할 수 없습니다.")
        String name,

        /**
         * 모임 커버 이미지 (선택)
         */
        String coverImageUrl,

        /**
         * 모임 공개 여부 (필수, GroupAccess Enum)
         * - PUBLIC, PRIVATE
         */
        GroupAccess access,

        /**
         * 모임 유형 (필수, GroupType Enum)
         * - ONLINE, OFFLINE, HYBRID
         */
        GroupType type,

        /**
         * 모임 관심사 (필수, GroupInterest Enum)
         * - HOBBY, FAMILY, SCHOOL, BUSINESS, EXERCISE, GAME, STUDY, FAN, OTHER
         */
        GroupInterest interest
) {}