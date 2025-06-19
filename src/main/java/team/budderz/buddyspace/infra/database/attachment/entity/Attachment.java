package team.budderz.buddyspace.infra.database.attachment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Getter
@Entity
@Table(name = "attachments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // S3에 저장된 실제 파일명 (디렉토리 포함된 key)
    @Column(nullable = false, unique = true)
    private String key;

    // 영상 파일의 썸네일 key
    @Column(name = "thumbnail_key", unique = true)
    private String thumbnailKey;

    // 원본 파일명 (업로드 시 사용자에게 보이던 이름)
    @Column(nullable = false)
    private String filename;

    // 파일 크기 (바이트 단위)
    @Column(nullable = false)
    private Long size;

    // 파일 유형 (image/jpeg, video/mp4, application/pdf 등)
    @Column(nullable = false)
    private String contentType;

    // 업로더
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private User uploader;

    // 생성자
    @Builder
    public Attachment(String key, String filename, Long size, String contentType, User uploader, String thumbnailKey) {
        this.key = key;
        this.filename = filename;
        this.size = size;
        this.contentType = contentType;
        this.uploader = uploader;
        this.thumbnailKey = thumbnailKey;
    }
}