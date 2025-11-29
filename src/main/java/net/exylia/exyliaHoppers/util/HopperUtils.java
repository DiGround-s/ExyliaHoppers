package net.exylia.exyliaHoppers.util;

import net.exylia.exyliaHoppers.cache.ChunkKey;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Set;

public class HopperUtils {
    public static Set<ChunkKey> getChunksInRange(World world, int chunkX, int chunkZ, int radius) {
        Set<ChunkKey> chunks = new HashSet<>();

        for (int x = chunkX - radius; x <= chunkX + radius; x++) {
            for (int z = chunkZ - radius; z <= chunkZ + radius; z++) {
                chunks.add(ChunkKey.of(world, x, z));
            }
        }

        return chunks;
    }

    public static Set<Chunk> getLoadedChunksInRange(World world, int chunkX, int chunkZ, int radius) {
        Set<Chunk> chunks = new HashSet<>();

        for (int x = chunkX - radius; x <= chunkX + radius; x++) {
            for (int z = chunkZ - radius; z <= chunkZ + radius; z++) {
                if (world.isChunkLoaded(x, z)) {
                    chunks.add(world.getChunkAt(x, z));
                }
            }
        }

        return chunks;
    }

    public static boolean isInventoryFull(Hopper hopper) {
        Inventory inv = hopper.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                return false;
            }
        }
        return true;
    }

    public static boolean isInventoryFull(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                return false;
            }
        }
        return true;
    }

    public static double getDistance(Location loc1, Location loc2) {
        return Math.sqrt(
                Math.pow(loc1.getX() - loc2.getX(), 2) +
                Math.pow(loc1.getY() - loc2.getY(), 2) +
                Math.pow(loc1.getZ() - loc2.getZ(), 2)
        );
    }

    public static double getDistanceSquared(Location loc1, Location loc2) {
        return Math.pow(loc1.getX() - loc2.getX(), 2) +
               Math.pow(loc1.getY() - loc2.getY(), 2) +
               Math.pow(loc1.getZ() - loc2.getZ(), 2);
    }
}
