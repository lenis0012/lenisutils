package com.lenis0012.pluginutils.updater;

import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * A service for checking plugin updates remotely.
 * Update checking is generally done periodically in the bacgkround
 * but the specifics depend on the specific implementaton.
 * <br/>
 * See {@link UpdaterFactory} for extended usage.
 */
public interface Updater {

    /**
     * Check the update status. This does not perform any actual version check.
     * Version checking is performed in the background unless otherwise stated by the implementation.
     *
     * @return Whether there is an update available
     */
    boolean isUpdateAvailable();

    /**
     * Get the information of the latest available update from the most recent version check.
     * This may return null if {@link #isUpdateAvailable()} is false and no version check has been performed yet.
     *
     * @return Version information (or null)
     */
    Version getLatestVersion();

    /**
     * Notify a player that an update is available if there is one.
     * This will send a message with interactive links to check the changelog, download the update or dismiss.
     *
     * @param player The recipient of the message
     */
    void notifyIfUpdateAvailable(Player player);

    /**
     * Manually download the latest available version asynchronously.
     * This may return a failed future if downloading is unsupported or no successful version check has been performed yet.
     *
     * @return A future with the installed version
     */
    default CompletableFuture<InstalledVersion> downloadLatestVersion() {
        CompletableFuture<InstalledVersion> future = new CompletableFuture<>();
        future.completeExceptionally(new UnsupportedOperationException());
        return future;
    }
}
