package team.budderz.buddyspace.api.notification.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.notification.entity.Notification;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;

import java.time.LocalDateTime;

@Schema(description = "알림 응답 DTO")
public record NotificationResponse(
        @Schema(description = "알림 식별자", example = "2")
        Long notificationId,

        @Schema(description = "알림 내용", example = "홍길동님이 새 게시글을 등록했습니다.")
        String content,

        @Schema(description = "알림 발생 화면 경로", example = "https://budderz.co.kr/page")
        String url,

        @Schema(description = "읽음 여부", example = "false")
        boolean isRead,

        @Schema(description = "모임 이름", example = "벗터즈")
        String groupName,

        @Schema(description = "알림 생성일시", example = "2025-06-17T16:40:27.9899751")
        LocalDateTime createdAt,

        @Schema(description = "알림 유형", example = "COMMENT")
        NotificationType type

) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getContent(),
                notification.getUrl(),
                notification.isRead(),
                notification.getGroup() != null ? notification.getGroup().getName() : null,
                notification.getCreatedAt(),
                notification.getType()
        );
    }
}
