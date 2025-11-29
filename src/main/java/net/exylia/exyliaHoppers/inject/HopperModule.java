package net.exylia.exyliaHoppers.inject;

import com.google.inject.AbstractModule;
import net.exylia.exyliaHoppers.ExyliaHoppers;
import net.exylia.exyliaHoppers.api.ExyliaHoppersAPI;
import net.exylia.exyliaHoppers.core.ChunkHopperService;
import net.exylia.exyliaHoppers.core.ChunkHopperServiceImpl;

public class HopperModule extends AbstractModule {
    private final ExyliaHoppers plugin;

    public HopperModule(ExyliaHoppers plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(ExyliaHoppers.class).toInstance(plugin);
        bind(ChunkHopperService.class).to(ChunkHopperServiceImpl.class);
        bind(ExyliaHoppersAPI.class).to(ChunkHopperServiceImpl.class);
    }
}
