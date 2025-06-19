package team.budderz.buddyspace.api.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.notification.response.FindsNotificationResponse;
import team.budderz.buddyspace.domain.notification.service.NotificationService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 전체 조회
    @GetMapping
    public BaseResponse<Page<FindsNotificationResponse>> findsNotice (
            @AuthenticationPrincipal UserAuth userAuth,
            Pageable pageable
    ) {
        Page<FindsNotificationResponse> response = notificationService.findsNotice(userAuth.getUserId(), pageable);
        return new BaseResponse<>(response);
    }

    // 알림 읽음 처리
    @PatchMapping("/{notificationId}/read")
    public BaseResponse<Void> readNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        notificationService.readNotification(notificationId, userAuth.getUserId());
        return new BaseResponse<>(null);
    }

    // 알림 개수 조회
    @GetMapping("/notice-count")
    public BaseResponse<Long> countUnreadNotifivation(
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long count = notificationService.countUnreadNotifications(userAuth.getUserId());
        return new BaseResponse<>(count);
    }

}
