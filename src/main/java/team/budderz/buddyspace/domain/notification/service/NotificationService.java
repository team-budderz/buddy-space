package team.budderz.buddyspace.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.notification.response.NotificationResponse;
import team.budderz.buddyspace.domain.notification.exception.NotificationErrorCode;
import team.budderz.buddyspace.global.exception.BaseException;
import team.budderz.buddyspace.infra.database.notification.entity.Notification;
import team.budderz.buddyspace.infra.database.notification.repository.NotificationRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationTemplateService templateService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> findsNotice(
            Long userId,
            Pageable pageable
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(NotificationErrorCode.USER_ID_NOT_FOUND));

        Page<Notification> notifications = notificationRepository.findAllByUserOrderByCreatedAtDesc(user, pageable);
        return  notifications
                .map(NotificationResponse::from);
    }
}
