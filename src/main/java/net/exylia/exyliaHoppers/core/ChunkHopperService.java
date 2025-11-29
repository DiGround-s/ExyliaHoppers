package net.exylia.exyliaHoppers.core;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import net.exylia.exyliaHoppers.api.ExyliaHoppersAPI;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ChunkHopperService extends ExyliaHoppersAPI {
    CompletableFuture<Boolean> registerHopper(Location location, HopperType type);

    CompletableFuture<Boolean> unregisterHopper(Location location);

    CompletableFuture<Optional<ChunkHopper>> getHopper(Location location);

    CompletableFuture<List<ChunkHopper>> getHoppersInChunk(Chunk chunk);

    CompletableFuture<List<ChunkHopper>> getAllHoppers();

    ItemStack createHopperItem(HopperType type, int amount);

    void invalidateHopperCache(Location location);

    void invalidateChunkCache(Chunk chunk);

    void clearAllCaches();

    CacheStats getHopperCacheStats();

    CacheStats getChunkCacheStats();

    CompletableFuture<Long> getTotalItemsCollected();

    CompletableFuture<Map<HopperType, Integer>> getHopperCountByType();

    boolean isVanillaAsChunk();

    int getTickInterval();

    int getMaxItemsPerTick();
}
