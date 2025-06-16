package team.budderz.buddyspace.domain.notification.service;

import org.springframework.stereotype.Service;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.domain.notification.exception.NotificationErrorCode;
import team.budderz.buddyspace.domain.notification.template.NotificationTemplate;
import team.budderz.buddyspace.global.exception.BaseException;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationTemplateService {

    private final Map<NotificationType, NotificationTemplate> templateMap = new HashMap<>();

    public NotificationTemplateService(List<NotificationTemplate> templates) {
        for (NotificationTemplate template : templates) {
            templateMap.put(template.getType(), template);
        }
    }

    public String generateContent(NotificationType type, NotificationArgs args) {
        NotificationTemplate template = templateMap.get(type);
        if(template == null) {
            throw new BaseException(NotificationErrorCode.UNSUPPORTED_NOTIFICATION_TYPE);
        }

        return template.generateContent(args);
    }
}
