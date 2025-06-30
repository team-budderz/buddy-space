package team.budderz.buddyspace.global.sse.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EmitterRepository {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // key = "userId_clientId"
    public void save(String id, SseEmitter emitter) {
        emitters.put(id, emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE 연결 완료: id={}, emitter={}", id, emitter);
            remove(id);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE 연결 타임아웃: id={}, emitter={}", id, emitter);
            remove(id);
        });
        emitter.onError((ex) -> {
            log.debug("SSE 연결 오류: id={}, emitter={}", id, emitter);
            remove(id);
        });
    }

    // userId 로 시작하는 모든 emitter 반환 (즉, 해당 유저의 모든 탭 emitter)
    public Set<SseEmitter> getAllByUserId(Long userId) {
        String prefix = userId + "_";
        return emitters.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    public void remove(String id) {
        SseEmitter emitter = emitters.remove(id);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("id = {} emitter 종료 중 에러: {}", id, e.getMessage());
            }
        }
    }

    // userId 로 시작하는 모든 emitter 삭제
    public void removeAllByUserId(Long userId) {
        String prefix = userId + "_";
        var toRemoveKeys = emitters.keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .collect(Collectors.toSet());

        for (String key : toRemoveKeys) {
            remove(key);
        }
    }

    public void removeByEmitter(SseEmitter emitter) {
        // emitters Map을 순회하며 emitter에 해당하는 key 찾기
        String keyToRemove = null;
        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            if (entry.getValue().equals(emitter)) {
                keyToRemove = entry.getKey();
                break;
            }
        }
        if (keyToRemove != null) {
            remove(keyToRemove);
        }
    }
}
