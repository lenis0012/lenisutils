package com.lenis0012.pluginutils.command.platform;

import com.lenis0012.pluginutils.command.api.message.MessageProcessor;
import com.lenis0012.pluginutils.command.wiring.CommandNode;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class BukkitPlatform implements Platform {
    private final Plugin plugin;

    public BukkitPlatform(Plugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public void registerCommands(Map<String, CommandNode> commandTree, MessageProcessor messageProcessor) {
        BukkitCommandExecutor executor = new BukkitCommandExecutor(commandTree, messageProcessor);
        commandTree.keySet().forEach(rootKey -> {
            String commandName = rootKey.substring(1);
            PluginCommand pluginCommand = plugin.getServer().getPluginCommand(commandName);
            if(pluginCommand == null) {
                throw new IllegalStateException("Command " + commandName + " not found in plugin yml!");
            }
            pluginCommand.setExecutor(executor);
            pluginCommand.setTabCompleter(executor);
        });
    }
}
