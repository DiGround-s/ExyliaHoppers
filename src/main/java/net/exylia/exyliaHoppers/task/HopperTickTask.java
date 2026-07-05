package net.exylia.exyliaHoppers.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.ExyliaHoppers;
import net.exylia.exyliaHoppers.api.event.HopperCollectItemEvent;
import net.exylia.exyliaHoppers.cache.ChunkItemCache;
import net.exylia.exyliaHoppers.cache.ChunkKey;
import net.exylia.exyliaHoppers.config.ConfigManager;
import net.exylia.exyliaHoppers.config.HopperConfig;
import net.exylia.exyliaHoppers.core.ChunkHopper;
import net.exylia.exyliaHoppers.core.ChunkHopperServiceImpl;
import net.exylia.exyliaHoppers.core.HopperRegistry;
import net.exylia.exyliaHoppers.integration.StackerManager;
import net.exylia.exyliaHoppers.integration.StackerProvider;
import net.exylia.exyliaHoppers.util.HopperUtils;
import net.exylia.exyliaHoppers.util.ItemUtils;
import net.exylia.exyliaHoppers.util.ThreadPoolManager;
import org.bukkit.*;
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
    private final StackerManager stackerManager;

    @Inject
    public HopperTickTask(
            ExyliaHoppers plugin,
            HopperRegistry registry,
            ChunkItemCache chunkItemCache,
            ConfigManager configManager,
            ThreadPoolManager threadPoolManager,
            ChunkHopperServiceImpl service,
            StackerManager stackerManager
    ) {
        this.plugin = plugin;
        this.registry = registry;
        this.chunkItemCache = chunkItemCache;
        this.configManager = configManager;
        this.threadPoolManager = threadPoolManager;
        this.service = service;
        this.stackerManager = stackerManager;
    }

    @Override
    public void run() {
        List<ChunkHopper> hoppers = registry.getAllHoppers();
        if (hoppers.isEmpty()) return;

        CompletableFuture.runAsync(() -> {
            for (ChunkHopper hopper : hoppers) {
                processHopper(hopper);
            }
        }, threadPoolManager.getForkJoinPool());
    }

    private void processHopper(ChunkHopper chunkHopper) {
        boolean debug = configManager.getConfig().isDebug();

        if (debug) {
            plugin.getLogger().info("=== PROCESSING HOPPER ===");
            plugin.getLogger().info("Location: " + chunkHopper.getLocation());
            plugin.getLogger().info("Type: " + chunkHopper.getType());
            plugin.getLogger().info("Chunks Radius: " + chunkHopper.getChunksRadius());
            plugin.getLogger().info("Hopper Chunk: [" + chunkHopper.getChunkX() + ", " + chunkHopper.getChunkZ() + "]");
        }

        if (!chunkHopper.getWorld().isChunkLoaded(chunkHopper.getChunkX(), chunkHopper.getChunkZ())) {
            if (debug) {
                plugin.getLogger().warning("Hopper chunk is not loaded!");
            }
            return;
        }

        Set<Chunk> chunksInRange = HopperUtils.getLoadedChunksInRange(
                chunkHopper.getWorld(),
                chunkHopper.getChunkX(),
                chunkHopper.getChunkZ(),
                chunkHopper.getChunksRadius()
        );

        if (debug) {
            plugin.getLogger().info("Chunks in range: " + chunksInRange.size());
            for (Chunk chunk : chunksInRange) {
                plugin.getLogger().info("  - Chunk [" + chunk.getX() + ", " + chunk.getZ() + "]");
            }
        }

        if (chunksInRange.isEmpty()) {
            if (debug) {
                plugin.getLogger().warning("No chunks found in range!");
            }
            return;
        }

        List<Item> items = new ArrayList<>();
        for (Chunk chunk : chunksInRange) {
            ChunkKey chunkKey = ChunkKey.of(chunk);
            Optional<List<Item>> cached = chunkItemCache.get(chunkKey);

            if (cached.isPresent()) {
                items.addAll(cached.get());
                if (debug) {
                    plugin.getLogger().info("Chunk [" + chunk.getX() + ", " + chunk.getZ() + "] (cached): " + cached.get().size() + " items");
                }
            } else {
                List<Item> chunkItems = ItemUtils.getItemsInChunk(chunk, debug);
                chunkItemCache.put(chunkKey, chunkItems);
                items.addAll(chunkItems);
                if (debug) {
                    plugin.getLogger().info("Chunk [" + chunk.getX() + ", " + chunk.getZ() + "] (fresh): " + chunkItems.size() + " items");
                }
            }
        }

        if (debug) {
            plugin.getLogger().info("Total items found in all chunks: " + items.size());
        }

        if (items.isEmpty()) return;

        Location hopperLoc = chunkHopper.getLocation();
        double maxCollectionDistance = calculateMaxDistance(chunkHopper.getChunksRadius());
        double maxDistSq = maxCollectionDistance * maxCollectionDistance;

        if (debug) {
            plugin.getLogger().info("Max collection distance: " + maxCollectionDistance);
            plugin.getLogger().info("Max distance squared: " + maxDistSq);
        }

        List<Item> itemsBeforeFilter = new ArrayList<>(items);
        items = items.stream()
                .filter(item -> {
                    double distSq = HopperUtils.getDistanceSquared(item.getLocation(), hopperLoc);
                    boolean withinRange = distSq <= maxDistSq;
                    if (debug && !withinRange) {
                        plugin.getLogger().info("Item filtered out - Distance: " + Math.sqrt(distSq) + " > " + maxCollectionDistance);
                        plugin.getLogger().info("  Item location: " + item.getLocation());
                        plugin.getLogger().info("  Hopper location: " + hopperLoc);
                    }
                    return withinRange;
                })
                .collect(Collectors.toList());

        if (debug) {
            plugin.getLogger().info("Items after distance filter: " + items.size() + "/" + itemsBeforeFilter.size());
        }

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

        if (debug) {
            plugin.getLogger().info("Final items to collect: " + items.size());
            plugin.getLogger().info("=========================");
        }

        collectItemsSync(chunkHopper, items);
    }

    private double calculateMaxDistance(int chunksRadius) {
        int configMaxDistance = configManager.getConfig().getMaxCollectionDistance();
        double requiredDistance = (chunksRadius * 2 + 1) * 16 * Math.sqrt(2);
        return Math.max(configMaxDistance, requiredDistance);
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

            StackerProvider provider = stackerManager.getProvider();

            for (Item item : items) {
                if (!item.isValid()) continue;

                HopperCollectItemEvent event = new HopperCollectItemEvent(chunkHopper, item);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) continue;

                int totalStackSize = provider.getStackSize(item);
                ItemStack itemStack = item.getItemStack();

                int remainingToCollect = totalStackSize;
                boolean collectedAny = false;

                while (remainingToCollect > 0 && !HopperUtils.isInventoryFull(inventory)) {
                    ItemStack toAdd = itemStack.clone();
                    toAdd.setAmount(Math.min(remainingToCollect, itemStack.getMaxStackSize()));

                    Map<Integer, ItemStack> remaining = inventory.addItem(toAdd);

                    int addedAmount = toAdd.getAmount();
                    if (!remaining.isEmpty()) {
                        addedAmount -= remaining.values().iterator().next().getAmount();
                    }

                    if (addedAmount > 0) {
                        remainingToCollect -= addedAmount;
                        collectedAny = true;
                        chunkHopper.incrementItemsCollected();
                        service.incrementTotalItemsCollected();
                    } else {
                        break;
                    }
                }

                if (collectedAny) {
                    playCollectionEffects(item.getLocation(), chunkHopper.getLocation());

                    if (remainingToCollect <= 0) {
                        item.remove();
                    } else {
                        provider.setStackSize(item, remainingToCollect);
                    }
                }

                if (HopperUtils.isInventoryFull(inventory)) {
                    break;
                }
            }
        });
    }

    private void playCollectionEffects(Location itemLocation, Location hopperLocation) {
        HopperConfig config = configManager.getConfig();
        if (!config.isEnableCollectionEffects()) return;

        World world = hopperLocation.getWorld();
        if (world == null) return;

        if (config.isEnableCollectionParticles()) {
            Location hopperTop = hopperLocation.clone().add(0.5, 0.9, 0.5);
            Location itemLoc = itemLocation.clone();

            world.spawnParticle(
                    Particle.FLAME,
                    itemLoc,
                    8,
                    0.15, 0.15, 0.15,
                    0.01
            );

            world.spawnParticle(
                    Particle.SOUL_FIRE_FLAME,
                    itemLoc,
                    5,
                    0.1, 0.1, 0.1,
                    0.02
            );

            world.spawnParticle(
                    Particle.END_ROD,
                    hopperTop,
                    12,
                    0.2, 0.05, 0.2,
                    0.03
            );

            world.spawnParticle(
                    Particle.CRIT_MAGIC,
                    hopperTop,
                    6,
                    0.25, 0.1, 0.25,
                    0.0
            );

            world.spawnParticle(
                    Particle.GLOW,
                    hopperTop,
                    3,
                    0.15, 0.05, 0.15,
                    0.01
            );

            world.spawnParticle(
                    Particle.ENCHANTMENT_TABLE,
                    hopperTop,
                    8,
                    0.3, 0.1, 0.3,
                    0.5
            );
        }

        if (config.isEnableCollectionSound()) {
            world.playSound(
                    itemLocation,
                    Sound.ENTITY_ITEM_PICKUP,
                    config.getSoundVolume() * 0.8f,
                    config.getSoundPitch() + 0.2f
            );

            world.playSound(
                    hopperLocation,
                    Sound.BLOCK_ENCHANTMENT_TABLE_USE,
                    config.getSoundVolume() * 0.3f,
                    1.8f
            );

            world.playSound(
                    hopperLocation,
                    Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                    config.getSoundVolume() * 0.4f,
                    1.6f
            );

            world.playSound(
                    hopperLocation,
                    Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                    config.getSoundVolume() * 0.5f,
                    1.4f
            );
        }
    }
}
