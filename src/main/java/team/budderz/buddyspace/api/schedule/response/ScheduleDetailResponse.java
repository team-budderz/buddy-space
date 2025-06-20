package team.budderz.buddyspace.api.schedule.response;

import team.budderz.buddyspace.infra.database.schedule.entity.Schedule;

import java.time.LocalDate;

public record ScheduleDetailResponse(
        String title,
        String content,
        String startAt,
        String endAt,
        String authorName,
        String authorImageUrl,
        LocalDate createdAt
) {
    public static ScheduleDetailResponse from(Schedule schedule, String authorImageUrl) {
        return new ScheduleDetailResponse(
                schedule.getTitle(),
                schedule.getContent(),
                String.valueOf(schedule.getStartAt()),
                String.valueOf(schedule.getEndAt()),
                schedule.getAuthor().getName(),
                authorImageUrl,
                schedule.getCreatedAt().toLocalDate()
        );
    }
}
