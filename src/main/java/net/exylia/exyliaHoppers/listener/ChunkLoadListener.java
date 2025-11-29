package net.exylia.exyliaHoppers.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.ExyliaHoppers;
import net.exylia.exyliaHoppers.cache.HopperLocationKey;
import net.exylia.exyliaHoppers.core.ChunkHopper;
import net.exylia.exyliaHoppers.core.ChunkHopperService;
import net.exylia.exyliaHoppers.core.ChunkHopperServiceImpl;
import net.exylia.exyliaHoppers.core.HopperRegistry;
import net.exylia.exyliaHoppers.core.HopperType;
import net.exylia.exyliaHoppers.util.PDCUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class ChunkLoadListener implements Listener {
    private final ExyliaHoppers plugin;
    private final ChunkHopperService service;
    private final HopperRegistry registry;

    @Inject
    public ChunkLoadListener(ExyliaHoppers plugin, ChunkHopperService service, HopperRegistry registry) {
        this.plugin = plugin;
        this.service = service;
        this.registry = registry;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        List<HopperData> hopperDataList = new ArrayList<>();

        for (BlockState state : chunk.getTileEntities()) {
            Block block = state.getBlock();
            if (block.getType() != Material.HOPPER) continue;

            Optional<HopperType> typeOpt = PDCUtils.getHopperType(block);

            HopperType type;
            int range;
            boolean needsSave = false;

            if (typeOpt.isPresent()) {
                type = typeOpt.get();
                range = PDCUtils.getHopperRange(block);
            } else if (service.isVanillaAsChunk()) {
                type = HopperType.VANILLA;
                range = ((ChunkHopperServiceImpl) service)
                        .getConfigManager().getConfig().getVanillaHopperChunkRadius();
                needsSave = true;
            } else {
                continue;
            }

            hopperDataList.add(new HopperData(block.getLocation(), type, range, needsSave));
        }

        if (!hopperDataList.isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (HopperData data : hopperDataList) {
                    if (data.needsSave) {
                        Block block = data.location.getBlock();
                        PDCUtils.setHopperData(block, data.type, data.range);
                    }

                    HopperLocationKey key = HopperLocationKey.of(data.location);
                    ChunkHopper hopper = ChunkHopper.builder()
                            .location(data.location)
                            .world(data.location.getWorld())
                            .x(data.location.getBlockX())
                            .y(data.location.getBlockY())
                            .z(data.location.getBlockZ())
                            .type(data.type)
                            .chunksRadius(data.range)
                            .placementTime(System.currentTimeMillis())
                            .build();

                    registry.register(key, hopper);
                }
            });
        }
    }

    private record HopperData(Location location, HopperType type, int range, boolean needsSave) {}
}
