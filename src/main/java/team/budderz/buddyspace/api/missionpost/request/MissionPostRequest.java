package team.budderz.buddyspace.api.missionpost.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "미션 인증 생성/수정 요청 DTO")
public record MissionPostRequest(
        @NotBlank
        @Size(max = 255)
        @Schema(description = "미션 인증 내용", example = "JAVA 알고리즘 문제 풀이 과정입니다.")
        String contents
) {
}
