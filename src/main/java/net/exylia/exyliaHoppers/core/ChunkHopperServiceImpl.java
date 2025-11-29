package net.exylia.exyliaHoppers.core;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.exylia.exyliaHoppers.cache.ChunkItemCache;
import net.exylia.exyliaHoppers.cache.ChunkKey;
import net.exylia.exyliaHoppers.cache.HopperCache;
import net.exylia.exyliaHoppers.cache.HopperLocationKey;
import net.exylia.exyliaHoppers.config.ConfigManager;
import net.exylia.exyliaHoppers.util.PDCUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Singleton
public class ChunkHopperServiceImpl implements ChunkHopperService {
    private final HopperRegistry registry;
    private final HopperCache hopperCache;
    private final ChunkItemCache chunkItemCache;
    @Getter
    private final ConfigManager configManager;

    private final AtomicLong totalItemsCollected = new AtomicLong(0);

    @Inject
    public ChunkHopperServiceImpl(
            HopperRegistry registry,
            HopperCache hopperCache,
            ChunkItemCache chunkItemCache,
            ConfigManager configManager
    ) {
        this.registry = registry;
        this.hopperCache = hopperCache;
        this.chunkItemCache = chunkItemCache;
        this.configManager = configManager;
    }

    @Override
    public CompletableFuture<Boolean> registerHopper(Location location, HopperType type) {
        return CompletableFuture.supplyAsync(() -> {
            HopperLocationKey key = HopperLocationKey.of(location);

            ChunkHopper hopper = ChunkHopper.builder()
                    .location(location)
                    .world(location.getWorld())
                    .x(location.getBlockX())
                    .y(location.getBlockY())
                    .z(location.getBlockZ())
                    .type(type)
                    .chunksRadius(type.getChunksRadius())
                    .placementTime(System.currentTimeMillis())
                    .build();

            registry.register(key, hopper);
            hopperCache.put(key, hopper);

            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> unregisterHopper(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            HopperLocationKey key = HopperLocationKey.of(location);
            registry.unregister(key);
            hopperCache.invalidate(key);
            return true;
        });
    }

    @Override
    public CompletableFuture<Optional<ChunkHopper>> getHopper(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            HopperLocationKey key = HopperLocationKey.of(location);

            Optional<ChunkHopper> cached = hopperCache.get(key);
            if (cached.isPresent()) {
                return cached;
            }

            ChunkHopper registered = registry.get(key);
            if (registered != null) {
                hopperCache.put(key, registered);
                return Optional.of(registered);
            }

            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<List<ChunkHopper>> getHoppersInChunk(Chunk chunk) {
        return CompletableFuture.supplyAsync(() -> {
            ChunkKey key = ChunkKey.of(chunk);
            return registry.getHoppersInChunk(key);
        });
    }

    @Override
    public CompletableFuture<List<ChunkHopper>> getAllHoppers() {
        return CompletableFuture.supplyAsync(registry::getAllHoppers);
    }

    @Override
    public ItemStack createHopperItem(HopperType type, int amount) {
        ItemStack item = new ItemStack(Material.HOPPER, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            var typeConfig = configManager.getConfig().getHopperTypes().get(type);
            if (typeConfig != null) {
                meta.displayName(net.kyori.adventure.text.Component.text(typeConfig.getDisplayName()));
            }
            item.setItemMeta(meta);
        }

        PDCUtils.setHopperData(item, type, type.getChunksRadius());
        return item;
    }

    @Override
    public void invalidateHopperCache(Location location) {
        HopperLocationKey key = HopperLocationKey.of(location);
        hopperCache.invalidate(key);
    }

    @Override
    public void invalidateChunkCache(Chunk chunk) {
        ChunkKey key = ChunkKey.of(chunk);
        chunkItemCache.invalidate(key);
    }

    @Override
    public void clearAllCaches() {
        hopperCache.clear();
        chunkItemCache.clear();
    }

    @Override
    public CacheStats getHopperCacheStats() {
        return hopperCache.stats();
    }

    @Override
    public CacheStats getChunkCacheStats() {
        return chunkItemCache.stats();
    }

    @Override
    public CompletableFuture<Long> getTotalItemsCollected() {
        return CompletableFuture.completedFuture(totalItemsCollected.get());
    }

    @Override
    public CompletableFuture<Map<HopperType, Integer>> getHopperCountByType() {
        return CompletableFuture.supplyAsync(() -> {
            Map<HopperType, Integer> counts = new ConcurrentHashMap<>();
            for (ChunkHopper hopper : registry.getAllHoppers()) {
                counts.merge(hopper.getType(), 1, Integer::sum);
            }
            return counts;
        });
    }

    @Override
    public boolean isVanillaAsChunk() {
        return configManager.getConfig().isVanillaHoppersAsChunk();
    }

    @Override
    public int getTickInterval() {
        return configManager.getConfig().getHopperTickInterval();
    }

    @Override
    public int getMaxItemsPerTick() {
        return configManager.getConfig().getMaxItemsPerTick();
    }

    public void incrementTotalItemsCollected() {
        totalItemsCollected.incrementAndGet();
    }
}
