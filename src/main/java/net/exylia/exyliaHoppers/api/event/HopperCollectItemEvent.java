package net.exylia.exyliaHoppers.api.event;

import lombok.Getter;
import lombok.Setter;
import net.exylia.exyliaHoppers.core.ChunkHopper;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class HopperCollectItemEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final ChunkHopper hopper;
    private final Item item;
    private boolean cancelled;

    public HopperCollectItemEvent(ChunkHopper hopper, Item item) {
        this.hopper = hopper;
        this.item = item;
        this.cancelled = false;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
