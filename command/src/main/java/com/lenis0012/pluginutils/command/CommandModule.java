package com.lenis0012.pluginutils.command;

import com.lenis0012.pluginutils.modules.ModularPlugin;
import com.lenis0012.pluginutils.modules.Module;

public class CommandModule extends Module<ModularPlugin> {
    public CommandModule(ModularPlugin plugin) {
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
