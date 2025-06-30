package team.budderz.buddyspace.global.sse.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team.budderz.buddyspace.global.security.UserAuth;
import team.budderz.buddyspace.global.sse.repository.EmitterRepository;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationSseController {

    private final EmitterRepository emitterRepository;
    private static final Long TIMEOUT = 60 * 60 * 1000L; // 1시간 유지

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal UserAuth userAuth,
            @RequestParam String clientId
    ) {
        Long userId = userAuth.getUserId();
        String emitterId = userId + "_" + clientId;
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitterRepository.save(emitterId, emitter);

        try {
            emitter.send(SseEmitter.event().name("connect").data("SSE 연결 완료"));
        } catch (IOException e) {
            emitterRepository.remove(emitterId);
            return emitter;
        }

        // heartbeat용 스케줄러
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (IOException e) {
                emitter.completeWithError(e);
                scheduler.shutdown();
            }
        }, 0, 15, TimeUnit.SECONDS);

        emitter.onCompletion(() -> {
            emitterRepository.remove(emitterId);
            scheduler.shutdown();
        });
        emitter.onTimeout(() -> {
            emitterRepository.remove(emitterId);
            scheduler.shutdown();
        });
        emitter.onError((e) -> {
            emitterRepository.remove(emitterId);
            scheduler.shutdown();
        });

        return emitter;
    }

}
