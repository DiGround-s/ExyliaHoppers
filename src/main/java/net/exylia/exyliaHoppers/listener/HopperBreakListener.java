package net.exylia.exyliaHoppers.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.core.ChunkHopperService;
import net.exylia.exyliaHoppers.core.HopperType;
import net.exylia.exyliaHoppers.util.PDCUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@Singleton
public class HopperBreakListener implements Listener {
    private final ChunkHopperService service;

    @Inject
    public HopperBreakListener(ChunkHopperService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHopperBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.HOPPER) return;

        Optional<HopperType> typeOpt = PDCUtils.getHopperType(block);
        if (typeOpt.isEmpty() && !service.isVanillaAsChunk()) return;

        service.unregisterHopper(block.getLocation());
        service.invalidateHopperCache(block.getLocation());

        if (typeOpt.isPresent() && typeOpt.get() != HopperType.VANILLA) {
            event.setDropItems(false);

            HopperType type = typeOpt.get();
            int range = PDCUtils.getHopperRange(block);

            ItemStack drop = service.createHopperItem(type, 1);
            block.getWorld().dropItemNaturally(block.getLocation(), drop);
        }
    }
}
