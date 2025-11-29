package net.exylia.exyliaHoppers.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class MessageUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component parse(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(parse(message));
    }

    public static Component success(String message) {
        return parse("<gradient:#8fffc1:#59a4ff>" + message + "</gradient>");
    }

    public static Component error(String message) {
        return parse("<gradient:#ff5959:#a33b53>" + message + "</gradient>");
    }

    public static Component warning(String message) {
        return parse("<gradient:#ffc58f:#ffaa5f>" + message + "</gradient>");
    }

    public static Component info(String message) {
        return parse("<gradient:#8a51c4:#aa76de>" + message + "</gradient>");
    }

    public static Component header(String title) {
        return parse("<gradient:#8a51c4:#b48fd9><bold>" + title + "</bold></gradient>");
    }

    public static Component footer() {
        return parse("<#b9a5cc>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</#b9a5cc>");
    }

    public static Component statLine(String key, String value, String color) {
        return parse("  <#b9a5cc>▸</#b9a5cc> <" + color + ">" + key + ":</" + color + "> <#e7cfff>" + value + "</#e7cfff>");
    }

    public static Component percentage(double value) {
        return parse("<#e7cfff><bold>" + String.format("%.1f", value) + "%</bold></#e7cfff>");
    }
}
