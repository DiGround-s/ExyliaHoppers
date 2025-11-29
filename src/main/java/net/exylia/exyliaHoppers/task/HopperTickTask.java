package net.exylia.exyliaHoppers.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.ExyliaHoppers;
import net.exylia.exyliaHoppers.api.event.HopperCollectItemEvent;
import net.exylia.exyliaHoppers.cache.ChunkItemCache;
import net.exylia.exyliaHoppers.cache.ChunkKey;
import net.exylia.exyliaHoppers.config.ConfigManager;
import net.exylia.exyliaHoppers.core.ChunkHopper;
import net.exylia.exyliaHoppers.core.ChunkHopperServiceImpl;
import net.exylia.exyliaHoppers.core.HopperRegistry;
import net.exylia.exyliaHoppers.util.HopperUtils;
import net.exylia.exyliaHoppers.util.ItemUtils;
import net.exylia.exyliaHoppers.util.ThreadPoolManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Singleton
public class HopperTickTask implements Runnable {
    private final ExyliaHoppers plugin;
    private final HopperRegistry registry;
    private final ChunkItemCache chunkItemCache;
    private final ConfigManager configManager;
    private final ThreadPoolManager threadPoolManager;
    private final ChunkHopperServiceImpl service;

    @Inject
    public HopperTickTask(
            ExyliaHoppers plugin,
            HopperRegistry registry,
            ChunkItemCache chunkItemCache,
            ConfigManager configManager,
            ThreadPoolManager threadPoolManager,
            ChunkHopperServiceImpl service
    ) {
        this.plugin = plugin;
        this.registry = registry;
        this.chunkItemCache = chunkItemCache;
        this.configManager = configManager;
        this.threadPoolManager = threadPoolManager;
        this.service = service;
    }

    @Override
    public void run() {
        List<ChunkHopper> hoppers = registry.getAllHoppers();
        if (hoppers.isEmpty()) return;

        List<CompletableFuture<Void>> futures = hoppers.stream()
                .map(this::processHopperAsync)
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private CompletableFuture<Void> processHopperAsync(ChunkHopper chunkHopper) {
        return CompletableFuture.runAsync(() -> {
            if (!chunkHopper.getWorld().isChunkLoaded(chunkHopper.getChunkX(), chunkHopper.getChunkZ())) {
                return;
            }

            Set<Chunk> chunksInRange = HopperUtils.getLoadedChunksInRange(
                    chunkHopper.getWorld(),
                    chunkHopper.getChunkX(),
                    chunkHopper.getChunkZ(),
                    chunkHopper.getChunksRadius()
            );

            if (chunksInRange.isEmpty()) return;

            List<Item> items = new ArrayList<>();
            for (Chunk chunk : chunksInRange) {
                ChunkKey chunkKey = ChunkKey.of(chunk);
                Optional<List<Item>> cached = chunkItemCache.get(chunkKey);

                if (cached.isPresent()) {
                    items.addAll(cached.get());
                } else {
                    List<Item> chunkItems = ItemUtils.getItemsInChunk(chunk);
                    chunkItemCache.put(chunkKey, chunkItems);
                    items.addAll(chunkItems);
                }
            }

            if (items.isEmpty()) return;

            Location hopperLoc = chunkHopper.getLocation();
            double maxDistSq = Math.pow(configManager.getConfig().getMaxCollectionDistance(), 2);

            items = items.stream()
                    .filter(item -> HopperUtils.getDistanceSquared(item.getLocation(), hopperLoc) <= maxDistSq)
                    .collect(Collectors.toList());

            if (items.isEmpty()) return;

            if (configManager.getConfig().isPrioritizeCloserItems()) {
                items.sort(Comparator.comparingDouble(item ->
                        HopperUtils.getDistanceSquared(item.getLocation(), hopperLoc)
                ));
            }

            int maxItems = configManager.getConfig().getMaxItemsPerTick();
            if (items.size() > maxItems) {
                items = items.subList(0, maxItems);
            }

            collectItemsSync(chunkHopper, items);
        }, threadPoolManager.getForkJoinPool());
    }

    private void collectItemsSync(ChunkHopper chunkHopper, List<Item> items) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Block block = chunkHopper.getWorld().getBlockAt(
                    chunkHopper.getX(),
                    chunkHopper.getY(),
                    chunkHopper.getZ()
            );

            if (block.getType() != Material.HOPPER) return;
            if (!(block.getState() instanceof Hopper hopper)) return;

            Inventory inventory = hopper.getInventory();
            if (HopperUtils.isInventoryFull(inventory)) return;

            for (Item item : items) {
                if (!item.isValid()) continue;

                HopperCollectItemEvent event = new HopperCollectItemEvent(chunkHopper, item);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) continue;

                ItemStack itemStack = item.getItemStack();
                Map<Integer, ItemStack> remaining = inventory.addItem(itemStack);

                if (remaining.isEmpty()) {
                    item.remove();
                    chunkHopper.incrementItemsCollected();
                    service.incrementTotalItemsCollected();
                } else {
                    int addedAmount = itemStack.getAmount();
                    for (ItemStack remainingStack : remaining.values()) {
                        addedAmount -= remainingStack.getAmount();
                    }

                    if (addedAmount > 0) {
                        ItemStack newStack = itemStack.clone();
                        newStack.setAmount(remaining.values().iterator().next().getAmount());
                        item.setItemStack(newStack);

                        chunkHopper.incrementItemsCollected();
                        service.incrementTotalItemsCollected();
                    }
                }

                if (HopperUtils.isInventoryFull(inventory)) {
                    break;
                }
            }
        });
    }
}
