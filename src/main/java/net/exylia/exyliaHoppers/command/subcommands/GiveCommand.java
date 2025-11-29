package net.exylia.exyliaHoppers.command.subcommands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.exylia.exyliaHoppers.core.ChunkHopperService;
import net.exylia.exyliaHoppers.core.HopperType;
import net.exylia.exyliaHoppers.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Singleton
public class GiveCommand {
    private final ChunkHopperService service;

    @Inject
    public GiveCommand(ChunkHopperService service) {
        this.service = service;
    }

    @Command("ehopper give")
    @CommandPermission("exyliahopper.give")
    public void give(Player sender, Player target, HopperType type, @Optional Integer amount) {
        if (type == HopperType.VANILLA) {
            sender.sendMessage(MessageUtil.error("Cannot give vanilla hoppers. Use CHUNK_1x1, CHUNK_3x3, etc."));
            return;
        }

        int finalAmount = amount != null ? amount : 1;
        if (finalAmount <= 0 || finalAmount > 64) {
            sender.sendMessage(MessageUtil.error("Amount must be between 1 and 64"));
            return;
        }

        ItemStack item = service.createHopperItem(type, finalAmount);
        target.getInventory().addItem(item);

        sender.sendMessage(MessageUtil.success("Given " + finalAmount + "x " + type.name() + " hopper to " + target.getName()));
        if (!sender.equals(target)) {
            target.sendMessage(MessageUtil.success("You received " + finalAmount + "x " + type.name() + " hopper"));
        }
    }
}
