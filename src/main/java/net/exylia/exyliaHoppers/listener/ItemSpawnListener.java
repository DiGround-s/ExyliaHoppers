package net.exylia.exyliaHoppers.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.ExyliaHoppers;
import net.exylia.exyliaHoppers.api.event.HopperCollectItemEvent;
import net.exylia.exyliaHoppers.cache.ChunkKey;
import net.exylia.exyliaHoppers.config.ConfigManager;
import net.exylia.exyliaHoppers.core.ChunkHopper;
import net.exylia.exyliaHoppers.core.ChunkHopperServiceImpl;
import net.exylia.exyliaHoppers.core.HopperRegistry;
import net.exylia.exyliaHoppers.util.HopperUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Singleton
public class ItemSpawnListener implements Listener {
    private final ExyliaHoppers plugin;
    private final HopperRegistry registry;
    private final ConfigManager configManager;
    private final ChunkHopperServiceImpl service;

    @Inject
    public ItemSpawnListener(
            ExyliaHoppers plugin,
            HopperRegistry registry,
            ConfigManager configManager,
            ChunkHopperServiceImpl service
    ) {
        this.plugin = plugin;
        this.registry = registry;
        this.configManager = configManager;
        this.service = service;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();

        if (item.getThrower() != null) {
            return;
        }

        Location itemLoc = item.getLocation();
        ChunkKey chunkKey = ChunkKey.of(itemLoc.getChunk());

        List<ChunkHopper> hoppersInChunk = registry.getHoppersInChunk(chunkKey);
        if (hoppersInChunk.isEmpty()) return;

        double maxDistSq = Math.pow(configManager.getConfig().getMaxCollectionDistance(), 2);

        ChunkHopper nearestHopper = hoppersInChunk.stream()
                .filter(h -> HopperUtils.getDistanceSquared(h.getLocation(), itemLoc) <= maxDistSq)
                .min(Comparator.comparingDouble(h -> HopperUtils.getDistanceSquared(h.getLocation(), itemLoc)))
                .orElse(null);

        if (nearestHopper == null) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!item.isValid()) return;

            Block block = nearestHopper.getWorld().getBlockAt(
                    nearestHopper.getX(),
                    nearestHopper.getY(),
                    nearestHopper.getZ()
            );

            if (block.getType() != Material.HOPPER) return;
            if (!(block.getState() instanceof Hopper hopper)) return;

            Inventory inventory = hopper.getInventory();
            if (HopperUtils.isInventoryFull(inventory)) return;

            HopperCollectItemEvent collectEvent = new HopperCollectItemEvent(nearestHopper, item);
            Bukkit.getPluginManager().callEvent(collectEvent);

            if (collectEvent.isCancelled()) return;

            ItemStack itemStack = item.getItemStack().clone();
            Map<Integer, ItemStack> remaining = inventory.addItem(itemStack);

            if (remaining.isEmpty()) {
                event.setCancelled(true);
                item.remove();
                nearestHopper.incrementItemsCollected();
                service.incrementTotalItemsCollected();
            } else {
                int originalAmount = itemStack.getAmount();
                int remainingAmount = 0;
                for (ItemStack remainingStack : remaining.values()) {
                    remainingAmount += remainingStack.getAmount();
                }

                int addedAmount = originalAmount - remainingAmount;
                if (addedAmount > 0) {
                    ItemStack newStack = itemStack.clone();
                    newStack.setAmount(remainingAmount);
                    item.setItemStack(newStack);

                    nearestHopper.incrementItemsCollected();
                    service.incrementTotalItemsCollected();
                }
            }
        });
    }
}
