package team.budderz.buddyspace.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    public void setData(String key, String value, long duration) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofMillis(duration));
    }

    public String getData(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void deleteData(String key) {
        stringRedisTemplate.delete(key);
    }
}
