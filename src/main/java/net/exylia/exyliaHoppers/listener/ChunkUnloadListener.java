package net.exylia.exyliaHoppers.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.cache.ChunkKey;
import net.exylia.exyliaHoppers.core.ChunkHopper;
import net.exylia.exyliaHoppers.core.ChunkHopperService;
import net.exylia.exyliaHoppers.core.HopperRegistry;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.List;

@Singleton
public class ChunkUnloadListener implements Listener {
    private final ChunkHopperService service;
    private final HopperRegistry registry;

    @Inject
    public ChunkUnloadListener(ChunkHopperService service, HopperRegistry registry) {
        this.service = service;
        this.registry = registry;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        ChunkKey key = ChunkKey.of(chunk);

        List<ChunkHopper> hoppers = List.copyOf(registry.getHoppersInChunk(key));
        for (ChunkHopper hopper : hoppers) {
            service.unregisterHopper(hopper.getLocation());
        }

        service.invalidateChunkCache(chunk);
    }
}
