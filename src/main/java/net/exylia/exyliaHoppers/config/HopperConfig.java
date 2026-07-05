package net.exylia.exyliaHoppers.config;

import lombok.Builder;
import lombok.Data;
import net.exylia.exyliaHoppers.core.HopperType;

import java.util.Map;

@Data
@Builder
public class HopperConfig {
    @Builder.Default
    private int hopperTickInterval = 8;

    @Builder.Default
    private int maxItemsPerTick = 64;

    @Builder.Default
    private boolean asyncChunkScan = true;

    @Builder.Default
    private int cacheTtlSeconds = 60;

    @Builder.Default
    private int corePoolSize = 4;

    @Builder.Default
    private int maxPoolSize = 8;

    @Builder.Default
    private int forkJoinParallelism = -1;

    @Builder.Default
    private boolean vanillaHoppersAsChunk = false;

    @Builder.Default
    private int vanillaHopperChunkRadius = 0;

    @Builder.Default
    private int maxCollectionDistance = 32;

    @Builder.Default
    private boolean prioritizeCloserItems = true;

    @Builder.Default
    private boolean debug = false;

    @Builder.Default
    private boolean statisticsEnabled = true;

    @Builder.Default
    private boolean enableCollectionEffects = true;

    @Builder.Default
    private boolean enableCollectionSound = true;

    @Builder.Default
    private boolean enableCollectionParticles = true;

    @Builder.Default
    private float soundVolume = 0.2f;

    @Builder.Default
    private float soundPitch = 1.5f;

    @Builder.Default
    private int particleCount = 5;

    private Map<HopperType, HopperTypeConfig> hopperTypes;

    @Data
    @Builder
    public static class HopperTypeConfig {
        private boolean enabled;
        private int chunksRadius;
        private String displayName;
    }
}
