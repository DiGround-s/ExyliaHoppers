package net.exylia.exyliaHoppers.core;

import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.cache.ChunkKey;
import net.exylia.exyliaHoppers.cache.HopperLocationKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class HopperRegistry {
    private final Map<HopperLocationKey, ChunkHopper> hoppers = new ConcurrentHashMap<>();
    private final Map<ChunkKey, List<ChunkHopper>> hoppersByChunk = new ConcurrentHashMap<>();

    public void register(HopperLocationKey key, ChunkHopper hopper) {
        hoppers.put(key, hopper);

        ChunkKey chunkKey = ChunkKey.of(hopper.getWorld(), hopper.getChunkX(), hopper.getChunkZ());
        hoppersByChunk.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(hopper);
    }

    public void unregister(HopperLocationKey key) {
        ChunkHopper hopper = hoppers.remove(key);
        if (hopper != null) {
            ChunkKey chunkKey = ChunkKey.of(hopper.getWorld(), hopper.getChunkX(), hopper.getChunkZ());
            List<ChunkHopper> chunkHoppers = hoppersByChunk.get(chunkKey);
            if (chunkHoppers != null) {
                chunkHoppers.remove(hopper);
                if (chunkHoppers.isEmpty()) {
                    hoppersByChunk.remove(chunkKey);
                }
            }
        }
    }

    public ChunkHopper get(HopperLocationKey key) {
        return hoppers.get(key);
    }

    public List<ChunkHopper> getHoppersInChunk(ChunkKey key) {
        return hoppersByChunk.getOrDefault(key, new ArrayList<>());
    }

    public List<ChunkHopper> getAllHoppers() {
        return new ArrayList<>(hoppers.values());
    }

    public void clear() {
        hoppers.clear();
        hoppersByChunk.clear();
    }

    public int size() {
        return hoppers.size();
    }
}
