package team.budderz.buddyspace.infra.client.s3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum S3Directory {
    PROFILE("profile"),
    GROUP_COVER("group-cover"),
    POST_IMAGE("attachments/post/image"),
    POST_VIDEO("attachments/post/video"),
    POST_FILE("attachments/post/file"),
    POST_THUMBNAIL("attachments/post/thumbnail");

    private final String path;
}
