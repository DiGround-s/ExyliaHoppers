package net.exylia.exyliaHoppers.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.inject.Singleton;
import org.bukkit.entity.Item;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class ChunkItemCache {
    private final Cache<ChunkKey, List<Item>> cache;

    public ChunkItemCache() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.SECONDS)
                .maximumSize(1000)
                .recordStats()
                .build();
    }

    public void put(ChunkKey key, List<Item> items) {
        cache.put(key, items);
    }

    public Optional<List<Item>> get(ChunkKey key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    public void invalidate(ChunkKey key) {
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
