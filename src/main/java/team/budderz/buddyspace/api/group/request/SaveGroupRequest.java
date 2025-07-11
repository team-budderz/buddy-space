package team.budderz.buddyspace.api.group.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import team.budderz.buddyspace.infra.database.group.entity.GroupAccess;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;
import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;

/**
 * 모임 생성 요청 DTO
 */
@Schema(description = "모임 생성 요청 DTO")
public record SaveGroupRequest(

        /**
         * 모임 이름 (필수, 최대 20자)
         */
        @NotBlank(message = "모임 이름은 필수입니다.")
        @Size(max = 20, message = "모임 이름은 20자를 초과할 수 없습니다.")
        @Schema(description = "모임 이름", example = "벗터즈")
        String name,

        /**
         * 모임 공개 여부 (필수, GroupAccess Enum)
         * - PUBLIC, PRIVATE
         */
        @NotNull(message = "모임 공개 여부는 필수입니다.")
        @Schema(description = "모임 공개 여부", example = "PUBLIC")
        GroupAccess access,

        /**
         * 모임 유형 (필수, GroupType Enum)
         * - ONLINE, OFFLINE, HYBRID
         */
        @NotNull(message = "모임 유형은 필수입니다.")
        @Schema(description = "모임 유형", example = "ONLINE")
        GroupType type,

        /**
         * 모임 관심사 (필수, GroupInterest Enum)
         * - HOBBY, FAMILY, SCHOOL, BUSINESS, EXERCISE, GAME, STUDY, FAN, OTHER
         */
        @NotNull(message = "모임 관심사는 필수입니다.")
        @Schema(description = "모임 관심사", example = "STUDY")
        GroupInterest interest
) {}