package net.exylia.exyliaHoppers.util;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemUtils {
    public static List<Item> getItemsInChunk(Chunk chunk) {
        return getItemsInChunk(chunk, false);
    }

    public static List<Item> getItemsInChunk(Chunk chunk, boolean debug) {
        Entity[] entities = chunk.getEntities();
        if (debug) {
            Bukkit.getLogger().info("    Total entities in chunk [" + chunk.getX() + ", " + chunk.getZ() + "]: " + entities.length);
        }

        List<Item> items = Arrays.stream(entities)
                .filter(entity -> entity instanceof Item)
                .map(entity -> (Item) entity)
                .filter(Entity::isValid)
                .collect(Collectors.toList());

        if (debug) {
            Bukkit.getLogger().info("    Valid items in chunk [" + chunk.getX() + ", " + chunk.getZ() + "]: " + items.size());
            for (Item item : items) {
                Bukkit.getLogger().info("      - " + item.getItemStack().getType() + " x" + item.getItemStack().getAmount() + " at " + item.getLocation());
            }
        }

        return items;
    }

    public static List<Item> getItemsInChunks(Iterable<Chunk> chunks) {
        List<Item> items = new ArrayList<>();
        for (Chunk chunk : chunks) {
            items.addAll(getItemsInChunk(chunk));
        }
        return items;
    }
}
