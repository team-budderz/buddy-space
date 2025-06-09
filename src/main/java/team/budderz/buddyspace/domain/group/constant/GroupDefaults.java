package team.budderz.buddyspace.domain.group.constant;

public final class GroupDefaults {

    private GroupDefaults() {}

    public static final String DEFAULT_INVITE_BASE_URL = "http://localhost:8080/api/invites?code="; // 테스트용

    public static final int DEFAULT_PAGE_SIZE = 100;

    public static final String DEFAULT_ONLINE_GROUP_COVER_IMAGE_URL = "https://raw.githubusercontent.com/withong/my-storage/main/budderz/icon-online-group.png";
    public static final String DEFAULT_OFFLINE_GROUP_COVER_IMAGE_URL = "https://raw.githubusercontent.com/withong/my-storage/main/budderz/icon-offline-group.png";
    public static final String DEFAULT_HYBRID_GROUP_COVER_IMAGE_URL = "https://raw.githubusercontent.com/withong/my-storage/main/budderz/icon-hybrid-group.png";
}
