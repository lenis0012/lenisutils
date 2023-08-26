package com.lenis0012.pluginutils.command.platform;

import com.lenis0012.pluginutils.command.api.message.MessageProcessor;
import com.lenis0012.pluginutils.command.wiring.CommandNode;

import java.util.Map;

public interface Platform {

    void registerCommands(Map<String, CommandNode> commandTree, MessageProcessor messageProcessor);
}
