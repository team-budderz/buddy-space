package team.budderz.buddyspace.api.schedule.response;

import team.budderz.buddyspace.infra.database.schedule.entity.Schedule;

public record ScheduleResponse(
	Long scheduleId,
	String title,
	String content,
	String startAt,
	String endAt,
	String authorName
) {
	public static ScheduleResponse from(Schedule schedule) {
		return new ScheduleResponse(
			schedule.getId(),
			schedule.getTitle(),
			schedule.getContent(),
			String.valueOf(schedule.getStartAt()),
			String.valueOf(schedule.getEndAt()),
			schedule.getAuthor().getName()
		);
	}
}
