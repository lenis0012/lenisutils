package com.lenis0012.pluginutils.command.api;

import net.md_5.bungee.api.chat.BaseComponent;

public interface CommandAuthor {

    boolean isPlayer();

    void sendMessage(String message);

    void sendBungeeChatMessage(BaseComponent[] components);
}
