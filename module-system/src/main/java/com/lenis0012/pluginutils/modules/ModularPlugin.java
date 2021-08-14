package com.lenis0012.pluginutils.modules;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class ModularPlugin extends JavaPlugin {
    private static ModularPlugin instance;

    private static void setInstance(ModularPlugin instance) {
        ModularPlugin.instance = instance;
    }

    public static ModularPlugin getInstance() {
        return instance;
    }

    protected final ModuleRegistry registry;

    public ModularPlugin(Class<? extends Module>... modules) {
        setInstance(this);
        this.registry = new ModuleRegistry(this, getClassLoader());
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

    public void reloadModules() {
        registry.reloadModules();
    }

    public <T extends Module> T getModule(Class<T> type) {
        return registry.getModule(type);
    }
}
