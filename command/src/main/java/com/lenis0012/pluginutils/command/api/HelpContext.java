package com.lenis0012.pluginutils.command.api;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.List;

public interface HelpContext {

    HelpContext getRoot();

    List<HelpEntry> getEntries();

    BaseComponent[] serialize(ChatColor highlightColor, ChatColor accentColor);

    interface HelpEntry {
        String getBaseCommand();

        String getSubCommand();

        String getDescription();

        BaseComponent[] serialize(ChatColor highlightColor);
    }
}
