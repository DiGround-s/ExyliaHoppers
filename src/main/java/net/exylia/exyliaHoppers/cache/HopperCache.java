package net.exylia.exyliaHoppers.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.config.ConfigManager;
import net.exylia.exyliaHoppers.core.ChunkHopper;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class HopperCache {
    private final Cache<HopperLocationKey, ChunkHopper> cache;

    @Inject
    public HopperCache(ConfigManager configManager) {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(configManager.getConfig().getCacheTtlSeconds(), TimeUnit.SECONDS)
                .maximumSize(5000)
                .recordStats()
                .build();
    }

    public void put(HopperLocationKey key, ChunkHopper hopper) {
        cache.put(key, hopper);
    }

    public Optional<ChunkHopper> get(HopperLocationKey key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    public void invalidate(HopperLocationKey key) {
        cache.invalidate(key);
    }

    public void clear() {
        cache.invalidateAll();
    }

    public CacheStats stats() {
        return cache.stats();
    }

    public long size() {
        return cache.estimatedSize();
    }
}
