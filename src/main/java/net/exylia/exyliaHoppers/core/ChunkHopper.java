package net.exylia.exyliaHoppers.core;

import lombok.Builder;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Data
@Builder
public class ChunkHopper {
    private final Location location;
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final HopperType type;
    private final int chunksRadius;
    private final long placementTime;

    @Builder.Default
    private final AtomicInteger itemsCollected = new AtomicInteger(0);

    @Builder.Default
    private final AtomicLong lastActivity = new AtomicLong(System.currentTimeMillis());

    public void incrementItemsCollected() {
        itemsCollected.incrementAndGet();
        updateActivity();
    }

    public void updateActivity() {
        lastActivity.set(System.currentTimeMillis());
    }

    public int getChunkX() {
        return x >> 4;
    }

    public int getChunkZ() {
        return z >> 4;
    }
}
