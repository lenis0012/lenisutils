package com.lenis0012.pluginutils;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class PluginHolder extends JavaPlugin {
    protected final Registry registry;

    public PluginHolder(Class<? extends Module>... modules) {
        this.registry = new Registry(this, getClassLoader());
        registry.registerModules(true, modules);
    }

    @Override
    public void onEnable() {
        registry.enableModules(true);
        enable();
        registry.enableModules(false);
    }

    @Override
    public void onDisable() {
        registry.disableModules(false);
        disable();
        registry.disableModules(true);
    }

    public abstract void enable();

    public abstract void disable();

    public <T extends Module> T getModule(Class<T> type) {
        return registry.getModule(type);
    }
}
