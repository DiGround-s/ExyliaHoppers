package net.exylia.exyliaHoppers.util;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemUtils {
    public static List<Item> getItemsInChunk(Chunk chunk) {
        return Arrays.stream(chunk.getEntities())
                .filter(entity -> entity instanceof Item)
                .map(entity -> (Item) entity)
                .filter(Entity::isValid)
                .collect(Collectors.toList());
    }

    public static List<Item> getItemsInChunks(Iterable<Chunk> chunks) {
        List<Item> items = new ArrayList<>();
        for (Chunk chunk : chunks) {
            items.addAll(getItemsInChunk(chunk));
        }
        return items;
    }
}
