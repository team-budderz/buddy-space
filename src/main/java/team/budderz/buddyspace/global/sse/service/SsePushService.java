package team.budderz.buddyspace.global.sse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.budderz.buddyspace.api.notification.response.NotificationResponse;
import team.budderz.buddyspace.global.sse.repository.EmitterRepository;
import team.budderz.buddyspace.infra.database.notification.entity.Notification;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SsePushService {

    private final EmitterRepository emitterRepository;

    public void send(Long userId, Notification notification) {
        log.info("SsePushService.send() 호출 - userId: {}", userId);

        Set<SseEmitter> emitters = emitterRepository.getAllByUserId(userId);
        if (emitters == null || emitters.isEmpty()) {
            log.info("Emitter 존재하지 않음");
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                log.info("SSE 이벤트 전송 시도");
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(NotificationResponse.from(notification))
                );
                log.info("SSE 이벤트 전송 성공");
            } catch (IOException e) {
                log.error("SSE 전송 실패, emitter 제거", e);
                // 역매핑이 없으면 emitter로 id를 못 찾으니, EmitterRepository에 removeByEmitter 메서드 필요
                emitterRepository.removeByEmitter(emitter);
            }
        }
    }
}
