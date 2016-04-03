package com.lenis0012.pluginutils;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.logging.Logger;

public abstract class Module<T extends PluginHolder> {
    protected T plugin;
    protected boolean enabled;
    boolean local;

    public Module(T plugin) {
        this.plugin = plugin;
    }

    public abstract void enable();

    public abstract void disable();

    public void reload() {}

    public List<Class<? extends Module>> getRequiredModules() {
        return Lists.newArrayList();
    }

    protected void register(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    protected void register(CommandExecutor command, String... cmds) {
        for(String cmd : cmds) {
            plugin.getCommand(cmd).setExecutor(command);
        }
    }

    protected Logger logger() {
        return plugin.getLogger();
    }
}
