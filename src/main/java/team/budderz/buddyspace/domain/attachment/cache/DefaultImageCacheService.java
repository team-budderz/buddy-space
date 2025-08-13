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
        String cached = null;
        try {
            cached = redis.opsForValue().get(key);
        } catch (RuntimeException e) {
            // Redis 읽기 실패 시: 캐시 미사용 폴백
        }

        if (cached != null) {
            return cached;
        }

        String loaded = loader.apply(s3Key);

        if (loaded != null) {
            try {
                redis.opsForValue().set(key, loaded, TTL);
            } catch (RuntimeException e) {
                // Redis 쓰기 실패 시: 결과만 반환
            }
        }

        return loaded;
    }

    public void evict(String s3Key) {
        redis.delete(key(s3Key));
    }
}
