package team.budderz.buddyspace.domain.attachment.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class DefaultImageCacheService {

    private final StringRedisTemplate redis;
    private static final Duration TTL = Duration.ofMinutes(55);

    private String key(String s3Key) {
        return "default:view:" + s3Key;
    }

    public String getOrLoad(String s3Key, Function<String, String> loader) {
        String key = key(s3Key);
        String cached = redis.opsForValue().get(key);

        if (cached != null) {
            return cached;
        }

        String loaded = loader.apply(s3Key);

        if (loaded != null) {
            redis.opsForValue().set(key, loaded, TTL);
        }

        return loaded;
    }

    public void evict(String s3Key) {
        redis.delete(key(s3Key));
    }
}
