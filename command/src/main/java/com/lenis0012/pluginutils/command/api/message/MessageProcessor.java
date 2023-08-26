package com.lenis0012.pluginutils.command.api.message;

import com.lenis0012.pluginutils.command.api.CommandContext;
import net.md_5.bungee.api.ChatColor;

@FunctionalInterface
public interface MessageProcessor {
    void process(CommandContext context, Message message, Object... args);

    MessageProcessor DEFAULT = (context, message, args) -> {
        if(message.isHelpMessage()) {
            context.getAuthor().sendBungeeChatMessage(context.getHelpContext().serialize(ChatColor.GREEN, ChatColor.DARK_GREEN));
            return;
        }
        context.getAuthor().sendMessage(ChatColor.RED + String.format(message.getTemplate(), args));
    };
}
