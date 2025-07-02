package team.budderz.buddyspace.api.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.notification.response.NotificationResponse;
import team.budderz.buddyspace.domain.notification.service.NotificationService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 전체 조회
    @Operation(summary = "알림 전체 조회", description = "사용자의 알림 목록을 페이지네이션 형태로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "알림 조회 성공",
            content = @Content(schema = @Schema(implementation = NotificationResponse.class)))
    @GetMapping
    public BaseResponse<Page<NotificationResponse>> findsNotice (
            @AuthenticationPrincipal UserAuth userAuth,
            Pageable pageable
    ) {
        Page<NotificationResponse> response = notificationService.findsNotice(userAuth.getUserId(), pageable);
        return new BaseResponse<>(response);
    }

    // 알림 읽음 처리
    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 처리합니다.")
    @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PatchMapping("/{notificationId}/read")
    public BaseResponse<Void> readNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        notificationService.readNotification(notificationId, userAuth.getUserId());
        return new BaseResponse<>(null);
    }

    // 알림 개수 조회
    @Operation(summary = "안 읽은 알림 개수 조회", description = "사용자의 읽지 않은 알림 개수를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "알림 개수 조회 성공",
            content = @Content(schema = @Schema(implementation = Long.class)))
    @GetMapping("/notice-count")
    public BaseResponse<Long> countUnreadNotification(
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long count = notificationService.countUnreadNotifications(userAuth.getUserId());
        return new BaseResponse<>(count);
    }

}
