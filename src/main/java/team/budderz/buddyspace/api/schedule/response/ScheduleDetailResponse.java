package team.budderz.buddyspace.api.schedule.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.schedule.entity.Schedule;

import java.time.LocalDate;

@Schema(description = "일정 상세 조회 응답 DTO")
public record ScheduleDetailResponse(
        @Schema(description = "일정 제목", example = "오늘 계획")
        String title,

        @Schema(description = "일정 내용", example = "길동이랑 밥 먹고 카페 가기")
        String content,

        @Schema(description = "일정 시작 일시", example = "2025-06-17T16:40")
        String startAt,

        @Schema(description = "일정 종료 일시", example = "2025-06-17T22:40")
        String endAt,

        @Schema(description = "생성자 이름", example = "김철수")
        String authorName,

        @Schema(description = "생성자 프로필 이미지 url", example = "https://profile.image")
        String authorImageUrl,

        @Schema(description = "일정 생성일자", example = "2025-06-25")
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
