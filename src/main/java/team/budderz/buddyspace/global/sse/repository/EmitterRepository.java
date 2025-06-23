package team.budderz.buddyspace.global.sse.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class EmitterRepository {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(Long userId, SseEmitter emitter) {
        remove(userId); // 기존 emitter이 있다면 정리

        // emitter 생명주기 콜백 등록
        emitter.onCompletion(() -> {
            log.debug("SSE 연결 완료: userId={}", userId);
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE 연결 타임아웃: userId={}", userId);
            emitters.remove(userId);
        });
        emitter.onError((ex) -> {
            log.debug("SSE 연결 오류: userId={}", userId);
            emitters.remove(userId);
        });

        emitters.put(userId, emitter);
    }

    public Optional<SseEmitter> get(Long userId) {
        return Optional.ofNullable(emitters.get(userId));
    }

    public void remove(Long userId) {
       SseEmitter emitter = emitters.remove(userId);
       if (emitter != null) {
           try {
               emitter.complete(); // 연결 종료
           } catch (Exception e) {
               log.warn("userId = {} 에 대한 SSE 연결 종료 처리 중 오류 발생: {}", userId, e.getMessage());
           }
       }
    }
}
