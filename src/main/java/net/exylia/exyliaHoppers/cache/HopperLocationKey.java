package net.exylia.exyliaHoppers.cache;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.concurrent.ConcurrentHashMap;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class HopperLocationKey {
    private static final ConcurrentHashMap<HopperLocationKey, HopperLocationKey> POOL = new ConcurrentHashMap<>();
    private static final int MAX_POOL_SIZE = 10000;

    private final String worldName;
    private final int x;
    private final int y;
    private final int z;

    public static HopperLocationKey of(Location location) {
        return of(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static HopperLocationKey of(World world, int x, int y, int z) {
        return of(world.getName(), x, y, z);
    }

    public static HopperLocationKey of(String worldName, int x, int y, int z) {
        HopperLocationKey key = new HopperLocationKey(worldName, x, y, z);
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
        return worldName + ":" + x + "," + y + "," + z;
    }
}
