package net.exylia.exyliaHoppers.integration.impl;

import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedItem;
import net.exylia.exyliaHoppers.integration.StackerProvider;
import org.bukkit.entity.Item;

public class RoseStackerProvider implements StackerProvider {
    private final RoseStackerAPI api;

    public RoseStackerProvider() {
        this.api = RoseStackerAPI.getInstance();
    }

    @Override
    public int getStackSize(Item item) {
        StackedItem stackedItem = api.getStackedItem(item);
        return stackedItem != null ? stackedItem.getStackSize() : item.getItemStack().getAmount();
    }

    @Override
    public void setStackSize(Item item, int amount) {
        StackedItem stackedItem = api.getStackedItem(item);
        if (stackedItem != null) {
            if (amount <= 0) {
                item.remove();
            } else {
                stackedItem.setStackSize(amount);
            }
        } else {
            if (amount <= 0) {
                item.remove();
            } else {
                item.getItemStack().setAmount(Math.min(amount, item.getItemStack().getMaxStackSize()));
            }
        }
    }

    @Override
    public boolean isStacked(Item item) {
        return api.getStackedItem(item) != null;
    }

    @Override
    public String getProviderName() {
        return "RoseStacker";
    }
}
