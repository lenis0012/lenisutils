package com.lenis0012.pluginutils.command.api;

import net.md_5.bungee.api.ChatColor;

@FunctionalInterface
public interface MessageProcessor {
    void process(CommandContext context, Message message, Object... args);

    MessageProcessor DEFAULT = (context, message, args) -> {
        if(message.isHelpMessage()) {
            context.getSender().spigot().sendMessage(context.getHelpContext().serialize(ChatColor.GREEN, ChatColor.DARK_GREEN));
            return;
        }
        context.getSender().sendMessage(ChatColor.RED + String.format(message.getTemplate(), args));
    };
}
