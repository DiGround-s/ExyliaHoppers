package net.exylia.exyliaHoppers.util;

import net.exylia.exyliaHoppers.ExyliaHoppers;
import net.exylia.exyliaHoppers.core.HopperType;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public class PDCUtils {
    private static ExyliaHoppers plugin;
    private static NamespacedKey TYPE_KEY;
    private static NamespacedKey RANGE_KEY;
    private static NamespacedKey PLACEMENT_TIME_KEY;
    private static NamespacedKey ITEMS_COLLECTED_KEY;

    public static void initialize(ExyliaHoppers pluginInstance) {
        plugin = pluginInstance;
        TYPE_KEY = new NamespacedKey(plugin, "hopper_type");
        RANGE_KEY = new NamespacedKey(plugin, "hopper_range");
        PLACEMENT_TIME_KEY = new NamespacedKey(plugin, "placement_time");
        ITEMS_COLLECTED_KEY = new NamespacedKey(plugin, "items_collected");
    }

    public static void setHopperData(ItemStack item, HopperType type, int range) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(TYPE_KEY, PersistentDataType.STRING, type.name());
        pdc.set(RANGE_KEY, PersistentDataType.INTEGER, range);

        item.setItemMeta(meta);
    }

    public static void setHopperData(Block block, HopperType type, int range) {
        if (!(block.getState() instanceof TileState state)) return;

        PersistentDataContainer pdc = state.getPersistentDataContainer();
        pdc.set(TYPE_KEY, PersistentDataType.STRING, type.name());
        pdc.set(RANGE_KEY, PersistentDataType.INTEGER, range);
        pdc.set(PLACEMENT_TIME_KEY, PersistentDataType.LONG, System.currentTimeMillis());
        pdc.set(ITEMS_COLLECTED_KEY, PersistentDataType.INTEGER, 0);

        state.update();
    }

    public static Optional<HopperType> getHopperType(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Optional.empty();

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String typeName = pdc.get(TYPE_KEY, PersistentDataType.STRING);
        if (typeName == null) return Optional.empty();

        return Optional.of(HopperType.fromString(typeName));
    }

    public static Optional<HopperType> getHopperType(Block block) {
        if (!(block.getState() instanceof TileState state)) return Optional.empty();

        PersistentDataContainer pdc = state.getPersistentDataContainer();
        String typeName = pdc.get(TYPE_KEY, PersistentDataType.STRING);
        if (typeName == null) return Optional.empty();

        return Optional.of(HopperType.fromString(typeName));
    }

    public static int getHopperRange(Block block) {
        if (!(block.getState() instanceof TileState state)) return 0;

        PersistentDataContainer pdc = state.getPersistentDataContainer();
        Integer range = pdc.get(RANGE_KEY, PersistentDataType.INTEGER);
        return range != null ? range : 0;
    }

    public static int getHopperRange(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer range = pdc.get(RANGE_KEY, PersistentDataType.INTEGER);
        return range != null ? range : 0;
    }

    public static void incrementItemsCollected(Block block) {
        if (!(block.getState() instanceof TileState state)) return;

        PersistentDataContainer pdc = state.getPersistentDataContainer();
        int current = pdc.getOrDefault(ITEMS_COLLECTED_KEY, PersistentDataType.INTEGER, 0);
        pdc.set(ITEMS_COLLECTED_KEY, PersistentDataType.INTEGER, current + 1);

        state.update();
    }

    public static int getItemsCollected(Block block) {
        if (!(block.getState() instanceof TileState state)) return 0;

        PersistentDataContainer pdc = state.getPersistentDataContainer();
        return pdc.getOrDefault(ITEMS_COLLECTED_KEY, PersistentDataType.INTEGER, 0);
    }

    public static boolean isChunkHopper(Block block) {
        return getHopperType(block).map(HopperType::isChunkHopper).orElse(false);
    }

    public static boolean isChunkHopper(ItemStack item) {
        return getHopperType(item).map(HopperType::isChunkHopper).orElse(false);
    }
}
