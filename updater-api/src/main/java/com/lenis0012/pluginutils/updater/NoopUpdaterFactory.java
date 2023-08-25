package com.lenis0012.pluginutils.updater;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

/**
 * A No-Op updater factory that acts as a fallback and does nothing.
 */
public class NoopUpdaterFactory implements UpdaterFactory {
    @Override
    public Updater getUpdater(Plugin plugin) {
        return new NoopUpdater();
    }

    @Override
    public boolean isCompatible(Plugin plugin) {
        return true;
    }

    @Override
    public UpdaterFactory withFrequency(Duration updateInterval) {
        return this;
    }

    @Override
    public Set<Capability> capabilities(Plugin plugin) {
        return Collections.emptySet();
    }

    private static class NoopUpdater implements Updater {

        @Override
        public boolean isUpdateAvailable() {
            return false;
        }

        @Override
        public Version getLatestVersion() {
            return null;
        }

        @Override
        public void notifyIfUpdateAvailable(Player player) {
        }
    }
}
