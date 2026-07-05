package net.exylia.exyliaHoppers.integration;

import org.bukkit.entity.Item;

public interface StackerProvider {
    int getStackSize(Item item);

    void setStackSize(Item item, int amount);

    boolean isStacked(Item item);

    String getProviderName();
}
