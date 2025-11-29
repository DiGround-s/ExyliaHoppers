package net.exylia.exyliaHoppers.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HopperType {
    VANILLA(0, "Vanilla Hopper"),
    CHUNK_1x1(0, "Chunk Hopper (1x1)"),
    CHUNK_3x3(1, "Chunk Hopper (3x3)"),
    CHUNK_5x5(2, "Chunk Hopper (5x5)"),
    CHUNK_7x7(3, "Chunk Hopper (7x7)");

    private final int chunksRadius;
    private final String defaultDisplayName;

    public static HopperType fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return VANILLA;
        }
    }

    public boolean isChunkHopper() {
        return this != VANILLA;
    }
}
