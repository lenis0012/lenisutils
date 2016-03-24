package com.lenis0012.pluginutils.modules.command;

import com.lenis0012.pluginutils.Module;
import com.lenis0012.pluginutils.PluginHolder;

public class CommandModule extends Module<PluginHolder> {
    public CommandModule(PluginHolder plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
    }

    public void registerCommand(Command command, String... aliases) {
        for(String alias : aliases) {
            plugin.getCommand(alias).setExecutor(command);
        }
    }
}
