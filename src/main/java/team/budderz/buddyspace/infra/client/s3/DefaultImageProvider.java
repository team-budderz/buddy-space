package team.budderz.buddyspace.infra.client.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;

@Component
@RequiredArgsConstructor
public class DefaultImageProvider {

    private final S3Service s3Service;

    @Value("${app.default.profile-image}")
    private String defaultProfileKey;

    @Value("${app.default.group-cover.online}")
    private String defaultGroupOnlineKey;

    @Value("${app.default.group-cover.offline}")
    private String defaultGroupOfflineKey;

    @Value("${app.default.group-cover.hybrid}")
    private String defaultGroupHybridKey;

    public String getDefaultProfileImageUrl() {
        return s3Service.generateViewUrl(defaultProfileKey);
    }

    public String getDefaultGroupCoverImageUrl(GroupType type) {
        String key = switch (type) {
            case ONLINE -> defaultGroupOnlineKey;
            case OFFLINE -> defaultGroupOfflineKey;
            case HYBRID -> defaultGroupHybridKey;
        };
        return s3Service.generateViewUrl(key);
    }

    public boolean isDefaultGroupCoverKey(String key) {
        return key.equals(defaultGroupOnlineKey) ||
                key.equals(defaultGroupOfflineKey) ||
                key.equals(defaultGroupHybridKey);
    }

    public boolean isDefaultProfileKey(String key) {
        return key.equals(defaultProfileKey);
    }
}
