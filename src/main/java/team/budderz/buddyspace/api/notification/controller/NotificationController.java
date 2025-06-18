package team.budderz.buddyspace.api.notification.controller;

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
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 전체 조회
    @GetMapping
    public BaseResponse<Page<NotificationResponse>> findsNotice (
            @AuthenticationPrincipal UserAuth userAuth,
            Pageable pageable
    ) {
        Long userId = userAuth.getUserId();
        Page<NotificationResponse> response = notificationService.findsNotice(userId, pageable);
        return new BaseResponse<>(response);
    }

    // 알림 읽음 처리


    // 알림 개수 조회

}
