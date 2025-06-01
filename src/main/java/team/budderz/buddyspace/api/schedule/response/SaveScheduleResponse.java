package team.budderz.buddyspace.api.schedule.response;

import team.budderz.buddyspace.infra.database.schedule.entity.Schedule;

public record SaveScheduleResponse (
	Long scheduleId
) {
	public static SaveScheduleResponse from(Schedule schedule) {
		return new SaveScheduleResponse(schedule.getId());
	}
}
