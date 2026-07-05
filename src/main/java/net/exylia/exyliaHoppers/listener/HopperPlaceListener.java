package net.exylia.exyliaHoppers.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.api.event.HopperPlaceEvent;
import net.exylia.exyliaHoppers.core.ChunkHopperService;
import net.exylia.exyliaHoppers.core.HopperType;
import net.exylia.exyliaHoppers.util.PDCUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@Singleton
public class HopperPlaceListener implements Listener {
    private final ChunkHopperService service;

    @Inject
    public HopperPlaceListener(ChunkHopperService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHopperPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getType() != Material.HOPPER) return;

        ItemStack item = event.getItemInHand();

        boolean debug = ((net.exylia.exyliaHoppers.core.ChunkHopperServiceImpl) service)
                .getConfigManager().getConfig().isDebug();

        if (debug) {
            Bukkit.getLogger().info("=== HOPPER PLACEMENT ===");
            Bukkit.getLogger().info("Player: " + event.getPlayer().getName());
            Bukkit.getLogger().info("Location: " + block.getLocation());
            Bukkit.getLogger().info("Item in hand: " + item);
            Bukkit.getLogger().info("Item meta: " + (item.getItemMeta() != null ? "Present" : "NULL"));
        }

        Optional<HopperType> typeOpt = PDCUtils.getHopperType(item);

        if (debug) {
            Bukkit.getLogger().info("PDC Type detected: " + (typeOpt.isPresent() ? typeOpt.get() : "NONE"));
            if (typeOpt.isPresent()) {
                Bukkit.getLogger().info("PDC Range: " + PDCUtils.getHopperRange(item));
            }
        }

        HopperType type;
        int range;

        if (typeOpt.isPresent()) {
            type = typeOpt.get();
            range = PDCUtils.getHopperRange(item);
            if (range == 0) {
                range = type.getChunksRadius();
            }
            if (debug) {
                Bukkit.getLogger().info("Using hopper from item PDC");
                Bukkit.getLogger().info("Final Type: " + type);
                Bukkit.getLogger().info("Final Range: " + range);
            }
        } else if (service.isVanillaAsChunk()) {
            type = HopperType.VANILLA;
            range = ((net.exylia.exyliaHoppers.core.ChunkHopperServiceImpl) service)
                    .getConfigManager().getConfig().getVanillaHopperChunkRadius();
            if (debug) {
                Bukkit.getLogger().info("Using vanilla-as-chunk mode");
                Bukkit.getLogger().info("Final Type: " + type);
                Bukkit.getLogger().info("Final Range: " + range);
            }
        } else {
            if (debug) {
                Bukkit.getLogger().info("No PDC found and vanilla-as-chunk is disabled. Not registering.");
            }
            return;
        }

        HopperPlaceEvent hopperPlaceEvent = new HopperPlaceEvent(event.getPlayer(), block, type);
        Bukkit.getPluginManager().callEvent(hopperPlaceEvent);

        if (hopperPlaceEvent.isCancelled()) {
            event.setCancelled(true);
            if (debug) {
                Bukkit.getLogger().info("HopperPlaceEvent was cancelled by another plugin");
            }
            return;
        }

        PDCUtils.setHopperData(block, type, range);
        service.registerHopper(block.getLocation(), type);

        if (debug) {
            Bukkit.getLogger().info("Hopper registered successfully");
            Bukkit.getLogger().info("========================");
        }
    }
}
