package net.exylia.exyliaHoppers.command;

import com.google.inject.Injector;
import net.exylia.exyliaHoppers.ExyliaHoppers;
import net.exylia.exyliaHoppers.command.subcommands.GiveCommand;
import net.exylia.exyliaHoppers.command.subcommands.ListCommand;
import net.exylia.exyliaHoppers.command.subcommands.ReloadCommand;
import net.exylia.exyliaHoppers.command.subcommands.StatsCommand;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public class HopperCommand {
    private final ExyliaHoppers plugin;
    private final Lamp<BukkitCommandActor> lamp;

    public HopperCommand(ExyliaHoppers plugin, Injector injector) {
        this.plugin = plugin;
        this.lamp = BukkitLamp.builder(plugin).build();
        registerCommands(injector);
    }

    private void registerCommands(Injector injector) {
        lamp.register(injector.getInstance(GiveCommand.class));
        lamp.register(injector.getInstance(ReloadCommand.class));
        lamp.register(injector.getInstance(StatsCommand.class));
        lamp.register(injector.getInstance(ListCommand.class));
    }
}
