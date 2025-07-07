package team.budderz.buddyspace.api.schedule.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import team.budderz.buddyspace.api.schedule.validator.ValidScheduleTime;

import java.time.LocalDateTime;

@Schema(description = "일정 생성 요청 DTO")
@ValidScheduleTime
public record SaveScheduleRequest (
	@NotBlank
	@Schema(description = "일정 제목", example = "오늘 계획")
	String title,

	@NotBlank
	@Schema(description = "일정 내용", example = "길동이랑 밥 먹고 카페 가기")
	String content,

	@NotNull
	@Schema(description = "일정 시작 일시", example = "2025-06-17T16:40:27.9899751")
	LocalDateTime startAt,

	@NotNull
	@Schema(description = "일정 종료 일시", example = "2025-06-17T22:40:27.9899751")
	LocalDateTime endAt
) {
}
