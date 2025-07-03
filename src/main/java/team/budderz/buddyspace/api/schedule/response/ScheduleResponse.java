package team.budderz.buddyspace.api.schedule.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.schedule.entity.Schedule;

@Schema(description = "일정 조회 응답 DTO")
public record ScheduleResponse(
        @Schema(description = "일정 식별자", example = "3")
        Long scheduleId,

        @Schema(description = "일정 제목", example = "개발하기")
        String title,

        @Schema(description = "일정 내용", example = "열심히 하기")
        String content,

        @Schema(description = "일정 시작 일시", example = "2025-06-17T16:40")
        String startAt,

        @Schema(description = "일정 종료 일시", example = "2025-06-17T22:40")
        String endAt,

        @Schema(description = "생성자 이름", example = "김철수")
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
