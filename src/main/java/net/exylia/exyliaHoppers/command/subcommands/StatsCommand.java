package net.exylia.exyliaHoppers.command.subcommands;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.core.ChunkHopperService;
import net.exylia.exyliaHoppers.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Singleton
public class StatsCommand {
    private final ChunkHopperService service;

    @Inject
    public StatsCommand(ChunkHopperService service) {
        this.service = service;
    }

    @Command("ehopper stats")
    @CommandPermission("exyliahopper.stats")
    public void stats(CommandSender sender) {
        CacheStats hopperStats = service.getHopperCacheStats();
        CacheStats chunkStats = service.getChunkCacheStats();

        sender.sendMessage(MessageUtil.header("Hopper Cache Statistics"));
        sender.sendMessage(Component.empty());

        MessageUtil.send(sender, "<gradient:#8a51c4:#b48fd9><bold>HOPPER CACHE</bold></gradient>");
        sender.sendMessage(MessageUtil.statLine("Hit Rate", "", "#8fffc1")
                .append(MessageUtil.percentage(hopperStats.hitRate() * 100)));
        MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#8fffc1>Hits:</#8fffc1> <#e7cfff><bold>" +
                formatNumber(hopperStats.hitCount()) + "</bold></#e7cfff>");
        MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#a33b53>Misses:</#a33b53> <#e7cfff><bold>" +
                formatNumber(hopperStats.missCount()) + "</bold></#e7cfff>");
        MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#59a4ff>Evictions:</#59a4ff> <#e7cfff><bold>" +
                formatNumber(hopperStats.evictionCount()) + "</bold></#e7cfff>");

        sender.sendMessage(Component.empty());

        MessageUtil.send(sender, "<gradient:#aa76de:#b48fd9><bold>CHUNK ITEM CACHE</bold></gradient>");
        sender.sendMessage(MessageUtil.statLine("Hit Rate", "", "#8fffc1")
                .append(MessageUtil.percentage(chunkStats.hitRate() * 100)));
        MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#8fffc1>Hits:</#8fffc1> <#e7cfff><bold>" +
                formatNumber(chunkStats.hitCount()) + "</bold></#e7cfff>");
        MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#a33b53>Misses:</#a33b53> <#e7cfff><bold>" +
                formatNumber(chunkStats.missCount()) + "</bold></#e7cfff>");

        sender.sendMessage(Component.empty());

        service.getTotalItemsCollected().thenAccept(total -> {
            MessageUtil.send(sender, "<gradient:#8a51c4:#b48fd9><bold>STATISTICS</bold></gradient>");
            MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#59a4ff>Total Items Collected:</#59a4ff> <#e7cfff><bold>" +
                    formatNumber(total) + "</bold></#e7cfff>");
            sender.sendMessage(Component.empty());
            sender.sendMessage(MessageUtil.footer());
        });
    }

    private String formatNumber(long number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }
}
