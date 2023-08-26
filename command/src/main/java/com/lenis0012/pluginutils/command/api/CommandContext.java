package com.lenis0012.pluginutils.command.api;


import lombok.Builder;
import lombok.Value;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Builder
@Value
public class CommandContext {
    CommandAuthor author;
    String command;
    String[] args;
    String label;
    HelpContext helpContext;
}
