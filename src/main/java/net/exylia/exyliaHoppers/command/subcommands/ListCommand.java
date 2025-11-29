package net.exylia.exyliaHoppers.command.subcommands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.core.ChunkHopper;
import net.exylia.exyliaHoppers.core.ChunkHopperService;
import net.exylia.exyliaHoppers.util.HopperUtils;
import net.exylia.exyliaHoppers.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ListCommand {
    private final ChunkHopperService service;

    @Inject
    public ListCommand(ChunkHopperService service) {
        this.service = service;
    }

    @Command("ehopper list")
    @CommandPermission("exyliahopper.list")
    public void list(Player sender, @Optional Integer radius) {
        int searchRadius = radius != null ? radius : 50;

        service.getAllHoppers().thenAccept(hoppers -> {
            List<ChunkHopper> nearbyHoppers = hoppers.stream()
                    .filter(h -> h.getWorld().equals(sender.getWorld()))
                    .filter(h -> HopperUtils.getDistance(h.getLocation(), sender.getLocation()) <= searchRadius)
                    .sorted((h1, h2) -> Double.compare(
                            HopperUtils.getDistance(h1.getLocation(), sender.getLocation()),
                            HopperUtils.getDistance(h2.getLocation(), sender.getLocation())
                    ))
                    .limit(10)
                    .collect(Collectors.toList());

            sender.sendMessage(MessageUtil.header("Nearby Chunk Hoppers"));
            sender.sendMessage(Component.empty());

            if (nearbyHoppers.isEmpty()) {
                MessageUtil.send(sender, "<#a33b53>No chunk hoppers found within " + searchRadius + " blocks</#a33b53>");
            } else {
                for (ChunkHopper hopper : nearbyHoppers) {
                    double distance = HopperUtils.getDistance(hopper.getLocation(), sender.getLocation());
                    MessageUtil.send(sender, "  <#b9a5cc>▸</#b9a5cc> <#59a4ff>" + hopper.getType().name() +
                            "</#59a4ff> <#8a51c4>at</#8a51c4> <#e7cfff>" + hopper.getX() + ", " + hopper.getY() + ", " + hopper.getZ() +
                            "</#e7cfff> <#8a51c4>(" + String.format("%.1f", distance) + "m)</#8a51c4>");
                    MessageUtil.send(sender, "    <#ffc58f>Items Collected:</#ffc58f> <#e7cfff>" +
                            hopper.getItemsCollected().get() + "</#e7cfff>");
                }
            }

            sender.sendMessage(Component.empty());
            sender.sendMessage(MessageUtil.footer());
        });
    }
}
