package team.budderz.buddyspace.infra.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team.budderz.buddyspace.domain.attachment.service.AttachmentService;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttachmentCleanupScheduler {

    private final AttachmentService attachmentService;

    @Scheduled(cron = "0 0 20 * * *")
    public void deleteOrphanAttachmentsDaily() {
        log.info("고아 첨부파일 삭제 스케줄 시작");
        Integer deleted = attachmentService.deleteOrphanAttachments();
        log.info("고아 첨부파일 {}건 삭제 완료", deleted);
    }
}
