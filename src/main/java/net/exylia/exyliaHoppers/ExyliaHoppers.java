package net.exylia.exyliaHoppers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import net.exylia.exyliaHoppers.api.ExyliaHoppersAPI;
import net.exylia.exyliaHoppers.cache.ChunkKey;
import net.exylia.exyliaHoppers.cache.HopperLocationKey;
import net.exylia.exyliaHoppers.command.HopperCommand;
import net.exylia.exyliaHoppers.config.ConfigManager;
import net.exylia.exyliaHoppers.core.ChunkHopperService;
import net.exylia.exyliaHoppers.core.ChunkHopperServiceImpl;
import net.exylia.exyliaHoppers.core.HopperRegistry;
import net.exylia.exyliaHoppers.inject.HopperModule;
import net.exylia.exyliaHoppers.listener.ChunkLoadListener;
import net.exylia.exyliaHoppers.listener.ChunkUnloadListener;
import net.exylia.exyliaHoppers.listener.HopperBreakListener;
import net.exylia.exyliaHoppers.listener.HopperPlaceListener;
import net.exylia.exyliaHoppers.listener.ItemSpawnListener;
import net.exylia.exyliaHoppers.task.TaskScheduler;
import net.exylia.exyliaHoppers.util.PDCUtils;
import net.exylia.exyliaHoppers.util.ThreadPoolManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExyliaHoppers extends JavaPlugin {
    @Getter
    private static ExyliaHoppers instance;

    private Injector injector;
    private ChunkHopperService service;
    private TaskScheduler taskScheduler;
    private ThreadPoolManager threadPoolManager;
    private HopperRegistry registry;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("=== ExyliaHoppers Startup ===");
        getLogger().info("[1/11] Initializing PDC utilities...");
        PDCUtils.initialize(this);

        getLogger().info("[2/11] Creating Guice injector...");
        injector = Guice.createInjector(new HopperModule(this));
        getLogger().info("[2/11] Guice injector created");

        getLogger().info("[3/11] Loading configuration...");
        ConfigManager configManager = injector.getInstance(ConfigManager.class);
        getLogger().info("[3/11] Configuration loaded");

        getLogger().info("[4/11] Initializing thread pools...");
        threadPoolManager = injector.getInstance(ThreadPoolManager.class);
        getLogger().info("[4/11] Thread pools initialized");

        getLogger().info("[5/11] Initializing caches...");
        getLogger().info("[5/11] Caches initialized");

        getLogger().info("[6/11] Initializing registry...");
        registry = injector.getInstance(HopperRegistry.class);
        getLogger().info("[6/11] Registry initialized");

        getLogger().info("[7/11] Initializing services...");
        service = injector.getInstance(ChunkHopperService.class);
        getLogger().info("[7/11] Services initialized");

        getLogger().info("[8/11] Registering listeners...");
        registerListeners();
        getLogger().info("[8/11] Listeners registered");

        getLogger().info("[9/11] Registering commands...");
        registerCommands();
        getLogger().info("[9/11] Commands registered");

        getLogger().info("[10/11] Starting task scheduler...");
        taskScheduler = injector.getInstance(TaskScheduler.class);
        taskScheduler.start();
        getLogger().info("[10/11] Task scheduler started");

        getLogger().info("[11/11] Registering API...");
        registerAPI();
        getLogger().info("[11/11] API registered");

        getLogger().info("=== ExyliaHoppers Enabled ===");
        getLogger().info("Hopper tick interval: " + configManager.getConfig().getHopperTickInterval() + " ticks");
        getLogger().info("Max items per tick: " + configManager.getConfig().getMaxItemsPerTick());
        getLogger().info("Vanilla hoppers as chunk: " + configManager.getConfig().isVanillaHoppersAsChunk());
    }

    @Override
    public void onDisable() {
        getLogger().info("=== ExyliaHoppers Shutdown ===");

        getLogger().info("[1/6] Stopping task scheduler...");
        if (taskScheduler != null) {
            try {
                taskScheduler.stop();
                getLogger().info("[1/6] Task scheduler stopped");
            } catch (Exception e) {
                getLogger().warning("[1/6] Error stopping task scheduler: " + e.getMessage());
            }
        }

        getLogger().info("[2/6] Shutting down thread pools...");
        if (threadPoolManager != null) {
            try {
                threadPoolManager.shutdown();
                getLogger().info("[2/6] Thread pools shutdown");
            } catch (Exception e) {
                getLogger().warning("[2/6] Error shutting down thread pools: " + e.getMessage());
            }
        }

        getLogger().info("[3/6] Clearing registry...");
        if (registry != null) {
            try {
                registry.clear();
                getLogger().info("[3/6] Registry cleared");
            } catch (Exception e) {
                getLogger().warning("[3/6] Error clearing registry: " + e.getMessage());
            }
        }

        getLogger().info("[4/6] Clearing caches...");
        if (service != null) {
            try {
                service.clearAllCaches();
                getLogger().info("[4/6] Caches cleared");
            } catch (Exception e) {
                getLogger().warning("[4/6] Error clearing caches: " + e.getMessage());
            }
        }

        getLogger().info("[5/6] Clearing object pools...");
        try {
            ChunkKey.clearPool();
            HopperLocationKey.clearPool();
            getLogger().info("[5/6] Object pools cleared");
        } catch (Exception e) {
            getLogger().warning("[5/6] Error clearing object pools: " + e.getMessage());
        }

        getLogger().info("[6/6] Unregistering API...");
        try {
            getServer().getServicesManager().unregisterAll(this);
            getLogger().info("[6/6] API unregistered");
        } catch (Exception e) {
            getLogger().warning("[6/6] Error unregistering API: " + e.getMessage());
        }

        getLogger().info("=== ExyliaHoppers Disabled ===");
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(injector.getInstance(HopperPlaceListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getInstance(HopperBreakListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getInstance(ChunkLoadListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getInstance(ChunkUnloadListener.class), this);
        Bukkit.getPluginManager().registerEvents(injector.getInstance(ItemSpawnListener.class), this);
    }

    private void registerCommands() {
        new HopperCommand(this, injector);
    }

    private void registerAPI() {
        getServer().getServicesManager().register(
                ExyliaHoppersAPI.class,
                service,
                this,
                ServicePriority.Normal
        );
    }

    public ExyliaHoppersAPI getAPI() {
        return service;
    }
}
