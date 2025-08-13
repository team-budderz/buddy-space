package team.budderz.buddyspace.domain.attachment.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresignedUrlCacheService {

    private final StringRedisTemplate redis;
    private static final Duration TTL = Duration.ofMinutes(55);

    private String key(Long attachmentId) {
        return "attachment:view:" + attachmentId;
    }

    /**
     * 단건 조회: 캐시 → 존재하지 않으면 생성 후 캐시
     */
    public String getOrLoad(Long attachmentId, Function<Long, String> loader) {
        String key = key(attachmentId);
        String cached = null;
        try {
            cached = redis.opsForValue().get(key);
        } catch (RuntimeException e) {
            // Redis 읽기 실패 시: 캐시 미사용 폴백
        }

        if (cached != null) {
            return cached;
        }

        String loaded = loader.apply(attachmentId);

        if (loaded != null) {
            try {
                redis.opsForValue().set(key, loaded, TTL);
            } catch (RuntimeException e) {
                // Redis 쓰기 실패 시: 결과만 반환
            }
        }

        return loaded;
    }

    public Map<Long, String> mgetOrLoad(Collection<Long> ids, Function<Long, String> loader) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        // 순회 순서 고정 및 null id 제거
        List<Long> idList = new ArrayList<>(ids);
        idList.removeIf(Objects::isNull);

        // 1. 키 목록 생성
        List<String> keys = idList.stream().map(this::key).toList();

        // 2. 캐시 일괄 조회 (방어: null 처리  Redis 장애 폴백)
        List<String> cached;
        try {
            cached = redis.opsForValue().multiGet(keys);
        } catch (RuntimeException e) {
            // Redis 장애 폴백: 로더로 전부 조회
            Map<Long, String> fallback = new HashMap<>(idList.size());
            for (Long id : idList) {
                String url = loader.apply(id);
                if (url != null) {
                    fallback.put(id, url);
                }
            }
            return fallback;
        }
        if (cached == null) {
            // 구현체에 따라 null 반환 가능성 → 요청 키 수만큼 null 리스트로 대체
            cached = Collections.nCopies(keys.size(), null);
        }

        Map<Long, String> result = new HashMap<>(ids.size());
        List<Long> misses = new ArrayList<>();

        // 3. 히트/미스 분류
        for (int i = 0; i < idList.size(); i++) {
            Long id = idList.get(i);
            String v = cached.get(i);
            if (v != null) {
                result.put(id, v);
            } else {
                misses.add(id);
            }
        }

        // 4. 미스에 대해 로더 호출 (null 값은 건너뜀: toMap 대신 수동 put)
        if (!misses.isEmpty()) {
            Map<Long, String> loaded = new HashMap<>(misses.size());
            for (Long id : misses) {
                String url = loader.apply(id);
                if (url != null) {
                    loaded.put(id, url);
                }
            }

            if (!loaded.isEmpty()) {
                try {
                    redis.executePipelined(new SessionCallback<Void>() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public Void execute(RedisOperations operations) throws DataAccessException {
                            // 제네릭 안전 캐스팅 (StringRedisTemplate 기반)
                            RedisOperations<String, String> ops = (RedisOperations<String, String>) operations;
                            ValueOperations<String, String> v = ops.opsForValue();

                            for (Map.Entry<Long, String> e : loaded.entrySet()) {
                                // key(id) 규칙을 그대로 사용하고, TTL(Duration)과 함께 저장
                                v.set(key(e.getKey()), e.getValue(), TTL);
                            }
                            return null;
                        }
                    });
                } catch (RuntimeException e) {
                    // Redis 쓰기 실패 시: 무시하고 결과만 반환
                }

                result.putAll(loaded);
            }
        }

        log.debug("presigned cache - requested: {}, hit: {}, miss: {}", ids.size(), ids.size() - misses.size(), misses.size());
        return result;
    }

    public void evict(Long attachmentId) {
        redis.delete(key(attachmentId));
    }
}
