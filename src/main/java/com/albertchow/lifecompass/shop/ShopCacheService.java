package com.albertchow.lifecompass.shop;

import com.albertchow.lifecompass.entity.Shop;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Read-through cache for shop detail lookups (the one endpoint hit often
 * enough per shop to be worth caching). Every write path that changes a
 * cached shop's fields — admin edits, a new/deleted rating recalculating the
 * score, a voucher purchase bumping the sold count — must call {@link #evict}
 * so a cache hit can never serve stale data; the TTL below is just a backstop,
 * not the primary correctness mechanism.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopCacheService {

    private static final String KEY_PREFIX = "shop:detail:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** Returns the cached shop, or null on a cache miss or any deserialization problem (treated the same as a miss). */
    public Shop get(Long id) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + id);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Shop.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached shop {}, treating as a cache miss", id, e);
            return null;
        }
    }

    /** Caches a shop for {@link #TTL}, silently skipping the write if serialization fails. */
    public void put(Shop shop) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + shop.getId(), objectMapper.writeValueAsString(shop), TTL);
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache shop {}", shop.getId(), e);
        }
    }

    /** Removes a shop from the cache; call this after any write that changes what getById(id) should return. */
    public void evict(Long id) {
        redisTemplate.delete(KEY_PREFIX + id);
    }
}
