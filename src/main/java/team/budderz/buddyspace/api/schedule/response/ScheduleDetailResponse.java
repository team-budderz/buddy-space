package team.budderz.buddyspace.api.schedule.response;

import java.time.LocalDate;

import team.budderz.buddyspace.infra.database.schedule.entity.Schedule;

public record ScheduleDetailResponse(
	String title,
	String content,
	String startAt,
	String endAt,
	String authorName,
	String authorImageUrl,
	LocalDate createdAt
) {
	public static ScheduleDetailResponse from(Schedule schedule) {
		return new ScheduleDetailResponse(
			schedule.getTitle(),
			schedule.getContent(),
			String.valueOf(schedule.getStartAt()),
			String.valueOf(schedule.getEndAt()),
			schedule.getAuthor().getName(),
			schedule.getAuthor().getImageUrl(),
			schedule.getCreatedAt().toLocalDate()
		);
	}
}
