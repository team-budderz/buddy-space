package team.budderz.buddyspace.global.sse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.budderz.buddyspace.api.notification.response.NotificationResponse;
import team.budderz.buddyspace.global.sse.repository.EmitterRepository;
import team.budderz.buddyspace.infra.database.notification.entity.Notification;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SsePushService {

    private final EmitterRepository emitterRepository;

    public void send(Long userId, Notification notification) {
        emitterRepository.get(userId).ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(NotificationResponse.from(notification))
                );
            } catch (IOException e) {
                emitterRepository.remove(userId);
            }
        });
    }
}
