package team.budderz.buddyspace.global.sse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.budderz.buddyspace.global.security.UserAuth;
import team.budderz.buddyspace.global.sse.repository.EmitterRepository;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationSseController {

    private final EmitterRepository emitterRepository;
    private static final Long TIMEOUT = 60 * 60 * 1000L; // 1시간 유지

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal UserAuth userAuth
            ) {
        Long userId = userAuth.getUserId();

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitterRepository.save(userId, emitter);

        // 연결 테스트용 초기 데이터 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결 완료")
            );
        } catch (IOException e) {
            emitterRepository.remove(userId);
        }

        // 연결 종료 시 정리
        emitter.onCompletion(() -> emitterRepository.remove(userId));
        emitter.onTimeout(() -> emitterRepository.remove(userId));
        emitter.onError((e) -> emitterRepository.remove(userId));

        return emitter;
    }
}
