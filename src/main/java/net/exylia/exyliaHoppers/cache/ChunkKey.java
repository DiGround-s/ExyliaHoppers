package net.exylia.exyliaHoppers.cache;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.ConcurrentHashMap;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class ChunkKey {
    private static final ConcurrentHashMap<ChunkKey, ChunkKey> POOL = new ConcurrentHashMap<>();
    private static final int MAX_POOL_SIZE = 5000;

    private final String worldName;
    private final int x;
    private final int z;

    public static ChunkKey of(Chunk chunk) {
        return of(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public static ChunkKey of(World world, int chunkX, int chunkZ) {
        return of(world.getName(), chunkX, chunkZ);
    }

    public static ChunkKey of(String worldName, int chunkX, int chunkZ) {
        ChunkKey key = new ChunkKey(worldName, chunkX, chunkZ);
        if (POOL.size() < MAX_POOL_SIZE) {
            return POOL.computeIfAbsent(key, k -> k);
        }
        return key;
    }

    public static void clearPool() {
        POOL.clear();
    }

    @Override
    public String toString() {
        return worldName + ":" + x + "," + z;
    }
}
