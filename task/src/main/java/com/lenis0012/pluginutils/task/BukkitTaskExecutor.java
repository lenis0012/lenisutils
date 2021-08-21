package com.lenis0012.pluginutils.task;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Executor;

public class BukkitTaskExecutor implements TaskExecutor {
    private final Plugin plugin;

    public BukkitTaskExecutor(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Executor sync() {
        return task -> Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public Executor async() {
        return task -> Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }
}
