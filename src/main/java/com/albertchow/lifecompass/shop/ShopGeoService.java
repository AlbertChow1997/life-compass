package com.albertchow.lifecompass.shop;

import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.mapper.ShopMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.GeoShape;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains a Redis GEO index of every shop's coordinates and answers
 * "what's near this point" queries against it. Indexing happens once at
 * startup (from whatever's already in MySQL) and incrementally whenever a
 * shop is created/updated, so this never needs a separate migration/sync job.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopGeoService implements ApplicationRunner {

    private static final String GEO_KEY = "shop:geo";

    private final StringRedisTemplate redisTemplate;
    private final ShopMapper shopMapper;

    /** Populates the GEO index from every shop already in MySQL when the app starts. */
    @Override
    public void run(ApplicationArguments args) {
        List<Shop> shops = shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                .isNotNull(Shop::getX)
                .isNotNull(Shop::getY));
        for (Shop shop : shops) {
            index(shop);
        }
        log.info("Indexed {} shops into the Redis GEO set", shops.size());
    }

    /** Adds/updates one shop's position in the GEO index; a no-op if it has no coordinates. */
    public void index(Shop shop) {
        if (shop.getX() == null || shop.getY() == null) {
            return;
        }
        geoOps().add(GEO_KEY, new Point(shop.getX().doubleValue(), shop.getY().doubleValue()), String.valueOf(shop.getId()));
    }

    /** Finds shop ids within radiusKm of (lat, lng), nearest first, mapped to their distance in km. */
    public Map<Long, Double> searchNearby(double lat, double lng, double radiusKm) {
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOps().search(
                GEO_KEY,
                GeoReference.fromCoordinate(lng, lat),
                GeoShape.byRadius(new Distance(radiusKm, Metrics.KILOMETERS)),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeDistance()
                        .sortAscending()
                        .limit(50));

        Map<Long, Double> distancesByShopId = new LinkedHashMap<>();
        if (results == null) {
            return distancesByShopId;
        }
        for (var result : results) {
            Long shopId = Long.valueOf(result.getContent().getName());
            distancesByShopId.put(shopId, result.getDistance().getValue());
        }
        return distancesByShopId;
    }

    private GeoOperations<String, String> geoOps() {
        return redisTemplate.opsForGeo();
    }
}
