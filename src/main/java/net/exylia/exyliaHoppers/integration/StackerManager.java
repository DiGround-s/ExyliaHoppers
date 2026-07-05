package net.exylia.exyliaHoppers.integration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.exylia.exyliaHoppers.ExyliaHoppers;
import net.exylia.exyliaHoppers.integration.impl.RoseStackerProvider;
import net.exylia.exyliaHoppers.integration.impl.VanillaStackerProvider;
import org.bukkit.Bukkit;

import java.util.logging.Level;

@Singleton
public class StackerManager {
    private final ExyliaHoppers plugin;

    @Getter
    private StackerProvider provider;

    @Inject
    public StackerManager(ExyliaHoppers plugin) {
        this.plugin = plugin;
        this.provider = new VanillaStackerProvider();
        detectStackerPlugin();
    }

    private void detectStackerPlugin() {
        if (Bukkit.getPluginManager().isPluginEnabled("RoseStacker")) {
            try {
                this.provider = new RoseStackerProvider();
                plugin.getLogger().log(Level.INFO, "Hooked into RoseStacker for item stacking support");
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to hook into RoseStacker, using vanilla stacking", e);
                this.provider = new VanillaStackerProvider();
            }
        } else {
            plugin.getLogger().log(Level.INFO, "No stacker plugin detected, using vanilla stacking");
        }
    }

    public void reload() {
        detectStackerPlugin();
    }
}
