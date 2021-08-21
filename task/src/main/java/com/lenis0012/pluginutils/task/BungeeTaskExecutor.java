package com.lenis0012.pluginutils.task;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.Executor;

public class BungeeTaskExecutor implements TaskExecutor {
    private final Plugin plugin;

    public BungeeTaskExecutor(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Executor sync() {
        throw new IllegalStateException("Bungeecord cannot run sync tasks, please use async tasks");
    }

    @Override
    public Executor async() {
        return task -> plugin.getProxy().getScheduler().runAsync(plugin, task);
    }
}
