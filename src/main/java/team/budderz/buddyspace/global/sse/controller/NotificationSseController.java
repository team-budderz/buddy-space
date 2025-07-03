package team.budderz.buddyspace.global.sse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "SSE", description = "SSE 관련 API")
public class NotificationSseController {

    private final EmitterRepository emitterRepository;
    private static final Long TIMEOUT = 60 * 60 * 1000L; // 1시간 유지

    @Operation(
            summary = "SSE 알림 구독",
            description = """
        클라이언트가 SSE 연결을 구독합니다. \s
        `clientId`는 브라우저 탭 또는 클라이언트 고유 식별자로 사용되며, 중복 방지를 위해 사용됩니다. \s
        연결 후 서버는 `connect`, `heartbeat` 이벤트를 주기적으로 전송합니다."""
    )
    @ApiResponse(responseCode = "200", description = "SSE 연결 성공", content = @Content(mediaType = "text/event-stream"))
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
