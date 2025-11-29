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
        Optional<HopperType> typeOpt = PDCUtils.getHopperType(item);

        HopperType type;
        int range;

        if (typeOpt.isPresent()) {
            type = typeOpt.get();
            range = PDCUtils.getHopperRange(item);
            if (range == 0) {
                range = type.getChunksRadius();
            }
        } else if (service.isVanillaAsChunk()) {
            type = HopperType.VANILLA;
            range = ((net.exylia.exyliaHoppers.core.ChunkHopperServiceImpl) service)
                    .getConfigManager().getConfig().getVanillaHopperChunkRadius();
        } else {
            return;
        }

        HopperPlaceEvent hopperPlaceEvent = new HopperPlaceEvent(event.getPlayer(), block, type);
        Bukkit.getPluginManager().callEvent(hopperPlaceEvent);

        if (hopperPlaceEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        PDCUtils.setHopperData(block, type, range);
        service.registerHopper(block.getLocation(), type);
    }
}
