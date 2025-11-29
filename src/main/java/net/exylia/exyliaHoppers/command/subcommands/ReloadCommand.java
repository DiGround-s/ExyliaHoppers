package net.exylia.exyliaHoppers.command.subcommands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.config.ConfigManager;
import net.exylia.exyliaHoppers.task.TaskScheduler;
import net.exylia.exyliaHoppers.util.MessageUtil;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.concurrent.CompletableFuture;

@Singleton
public class ReloadCommand {
    private final ConfigManager configManager;
    private final TaskScheduler taskScheduler;

    @Inject
    public ReloadCommand(ConfigManager configManager, TaskScheduler taskScheduler) {
        this.configManager = configManager;
        this.taskScheduler = taskScheduler;
    }

    @Command("ehopper reload")
    @CommandPermission("exyliahopper.reload")
    public void reload(CommandSender sender) {
        MessageUtil.send(sender, "<gradient:#8a51c4:#aa76de>Reloading configuration...</gradient>");

        CompletableFuture.runAsync(() -> {
            configManager.reloadConfig();
            taskScheduler.restart();
        }).thenRun(() -> {
            sender.sendMessage(MessageUtil.success("Configuration reloaded successfully!"));
            MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#59a4ff>Tick Interval:</#59a4ff> <#e7cfff>" +
                configManager.getConfig().getHopperTickInterval() + " ticks</#e7cfff>");
            MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#59a4ff>Max Items/Tick:</#59a4ff> <#e7cfff>" +
                configManager.getConfig().getMaxItemsPerTick() + "</#e7cfff>");
            MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#59a4ff>Vanilla as Chunk:</#59a4ff> <#e7cfff>" +
                configManager.getConfig().isVanillaHoppersAsChunk() + "</#e7cfff>");
            if (configManager.getConfig().isVanillaHoppersAsChunk()) {
                MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#59a4ff>Vanilla Chunk Radius:</#59a4ff> <#e7cfff>" +
                    configManager.getConfig().getVanillaHopperChunkRadius() + "</#e7cfff>");
            }
        });
    }
}
