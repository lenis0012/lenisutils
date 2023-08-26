package com.lenis0012.pluginutils.command.platform;

import com.lenis0012.pluginutils.command.api.CommandAuthor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitCommandAuthor implements CommandAuthor {
    private final CommandSender sender;

    BukkitCommandAuthor(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    @Override
    public void sendBungeeChatMessage(BaseComponent[] components) {
        sender.spigot().sendMessage(components);
    }

    public CommandSender getSender() {
        return sender;
    }
}
