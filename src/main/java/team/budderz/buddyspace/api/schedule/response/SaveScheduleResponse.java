package team.budderz.buddyspace.api.schedule.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.schedule.entity.Schedule;

@Schema(description = "일정 생성 응답 DTO")
public record SaveScheduleResponse (
		@Schema(description = "일정 식별자", example = "1")
		Long scheduleId
) {
	public static SaveScheduleResponse from(Schedule schedule) {
		return new SaveScheduleResponse(schedule.getId());
	}
}
