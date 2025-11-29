package net.exylia.exyliaHoppers.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.exylia.exyliaHoppers.ExyliaHoppers;
import net.exylia.exyliaHoppers.core.HopperType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;

@Singleton
public class ConfigManager {
    private final ExyliaHoppers plugin;

    @Getter
    private HopperConfig config;

    @Inject
    public ConfigManager(ExyliaHoppers plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration cfg = plugin.getConfig();

        Map<HopperType, HopperConfig.HopperTypeConfig> hopperTypes = new EnumMap<>(HopperType.class);
        ConfigurationSection typesSection = cfg.getConfigurationSection("hopper-types");
        if (typesSection != null) {
            for (HopperType type : HopperType.values()) {
                if (type == HopperType.VANILLA) continue;

                ConfigurationSection typeSection = typesSection.getConfigurationSection(type.name());
                if (typeSection != null) {
                    hopperTypes.put(type, HopperConfig.HopperTypeConfig.builder()
                            .enabled(typeSection.getBoolean("enabled", true))
                            .chunksRadius(typeSection.getInt("chunks-radius", type.getChunksRadius()))
                            .displayName(typeSection.getString("display-name", type.getDefaultDisplayName()))
                            .build());
                }
            }
        }

        this.config = HopperConfig.builder()
                .hopperTickInterval(cfg.getInt("hopper-tick-interval", 8))
                .maxItemsPerTick(cfg.getInt("max-items-per-tick", 64))
                .asyncChunkScan(cfg.getBoolean("async-chunk-scan", true))
                .cacheTtlSeconds(cfg.getInt("cache-ttl-seconds", 60))
                .corePoolSize(cfg.getInt("thread-pool.core-size", 4))
                .maxPoolSize(cfg.getInt("thread-pool.max-size", 8))
                .forkJoinParallelism(cfg.getInt("thread-pool.fork-join-parallelism", -1))
                .vanillaHoppersAsChunk(cfg.getBoolean("vanilla-hoppers-as-chunk", false))
                .vanillaHopperChunkRadius(cfg.getInt("vanilla-hopper-chunk-radius", 0))
                .maxCollectionDistance(cfg.getInt("max-collection-distance", 32))
                .prioritizeCloserItems(cfg.getBoolean("prioritize-closer-items", true))
                .debug(cfg.getBoolean("debug", false))
                .statisticsEnabled(cfg.getBoolean("statistics-enabled", true))
                .hopperTypes(hopperTypes)
                .build();
    }

    public void reloadConfig() {
        loadConfig();
    }
}
