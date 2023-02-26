package com.lenis0012.pluginutils.updater;

import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public interface Updater {

    boolean isUpdateAvailable();

    Version getLatestVersion();

    CompletableFuture<InstalledVersion> downloadLatestVersion();

    void notifyIfUpdateAvailable(Player player);
}
