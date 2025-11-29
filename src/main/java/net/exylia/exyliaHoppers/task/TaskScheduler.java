package net.exylia.exyliaHoppers.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.exylia.exyliaHoppers.ExyliaHoppers;
import net.exylia.exyliaHoppers.config.ConfigManager;
import org.bukkit.scheduler.BukkitTask;

@Singleton
public class TaskScheduler {
    private final ExyliaHoppers plugin;
    private final ConfigManager configManager;
    private final HopperTickTask hopperTickTask;

    @Getter
    private BukkitTask currentTask;

    @Inject
    public TaskScheduler(
            ExyliaHoppers plugin,
            ConfigManager configManager,
            HopperTickTask hopperTickTask
    ) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.hopperTickTask = hopperTickTask;
    }

    public void start() {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel();
        }

        int interval = configManager.getConfig().getHopperTickInterval();
        currentTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                hopperTickTask,
                interval,
                interval
        );
    }

    public void stop() {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel();
            currentTask = null;
        }
    }

    public void restart() {
        stop();
        start();
    }
}
