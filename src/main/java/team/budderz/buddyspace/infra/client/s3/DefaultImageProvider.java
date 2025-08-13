package team.budderz.buddyspace.infra.client.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import team.budderz.buddyspace.domain.attachment.cache.DefaultImageCacheService;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;

@Component
@RequiredArgsConstructor
public class DefaultImageProvider {

    private final S3Service s3Service;
    private final DefaultImageCacheService cacheService;

    @Value("${app.default.profile-image}")
    private String defaultProfileKey;

    @Value("${app.default.group-cover.online}")
    private String defaultGroupOnlineKey;

    @Value("${app.default.group-cover.offline}")
    private String defaultGroupOfflineKey;

    @Value("${app.default.group-cover.hybrid}")
    private String defaultGroupHybridKey;

    public String getDefaultProfileImageUrl() {
        return cacheService.getOrLoad(defaultProfileKey, s3Service::generateViewUrl);
    }

    public String getDefaultGroupCoverImageUrl(GroupType type) {
        String key = switch (type) {
            case ONLINE -> defaultGroupOnlineKey;
            case OFFLINE -> defaultGroupOfflineKey;
            case HYBRID -> defaultGroupHybridKey;
        };
        return cacheService.getOrLoad(key, s3Service::generateViewUrl);
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
