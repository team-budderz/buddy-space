package team.budderz.buddyspace.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.redisson.client.protocol.decoder.StreamGroupInfoDecoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.notification.response.FindsNotificationResponse;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.api.notification.response.NotificationResponse;
import team.budderz.buddyspace.domain.notification.exception.NotificationErrorCode;
import team.budderz.buddyspace.global.exception.BaseException;
import team.budderz.buddyspace.global.sse.service.SsePushService;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.notification.entity.Notification;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;
import team.budderz.buddyspace.infra.database.notification.repository.NotificationRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationTemplateService templateService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SsePushService ssePushService;

    // 알림 보내기
    @Transactional
    public void sendNotice(
            NotificationType type,
            User receiver,
            Group group,
            NotificationArgs args
    ) {
        String content = templateService.generateContent(type, args);
        String url = templateService.generateUrl(type, args);

        Notification notification = Notification.builder()
                .user(receiver)
                .group(group)
                .type(type)
                .content(content)
                .url(url)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        ssePushService.send(receiver.getId(), notification);
    }

    // 알림 모음
    @Transactional(readOnly = true)
    public Page<FindsNotificationResponse> findsNotice(
            Long userId,
            Pageable pageable
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(NotificationErrorCode.USER_ID_NOT_FOUND));

        return notificationRepository.findAllByUserOrderByCreatedAtDesc(user, pageable)
                .map(FindsNotificationResponse::from);
    }

    // 알림 읽음 처리
    @Transactional
    public void readNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BaseException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if(notification.doesNotBelongToUser(userId)) {
            throw new BaseException(NotificationErrorCode.NO_AUTH_TO_READ_NOTIFICATION);
        }
        notification.markAsRead();
    }

    // 알림 개수 조회
    @Transactional(readOnly = true)
    public Long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
}
