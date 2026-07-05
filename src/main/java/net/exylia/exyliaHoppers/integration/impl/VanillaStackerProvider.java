package net.exylia.exyliaHoppers.integration.impl;

import net.exylia.exyliaHoppers.integration.StackerProvider;
import org.bukkit.entity.Item;

public class VanillaStackerProvider implements StackerProvider {
    @Override
    public int getStackSize(Item item) {
        return item.getItemStack().getAmount();
    }

    @Override
    public void setStackSize(Item item, int amount) {
        if (amount <= 0) {
            item.remove();
        } else {
            item.getItemStack().setAmount(Math.min(amount, item.getItemStack().getMaxStackSize()));
        }
    }

    @Override
    public boolean isStacked(Item item) {
        return false;
    }

    @Override
    public String getProviderName() {
        return "Vanilla";
    }
}
