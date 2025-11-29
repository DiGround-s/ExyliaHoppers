package net.exylia.exyliaHoppers.api.event;

import lombok.Getter;
import lombok.Setter;
import net.exylia.exyliaHoppers.core.HopperType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class HopperPlaceEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Block block;
    private final HopperType type;
    private boolean cancelled;

    public HopperPlaceEvent(Player player, Block block, HopperType type) {
        this.player = player;
        this.block = block;
        this.type = type;
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
