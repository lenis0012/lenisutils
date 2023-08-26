package com.lenis0012.pluginutils.updater.bukkit;

import com.lenis0012.pluginutils.updater.Updater;
import com.lenis0012.pluginutils.updater.UpdaterFactory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BukkitUpdaterFactory implements UpdaterFactory {
    private Duration frequency = DEFAULT_FREQUENCY;

    @Override
    public Updater getUpdater(Plugin plugin) {
        return new BukkitUpdater(plugin, frequency);
    }

    @Override
    public boolean isCompatible(Plugin plugin) {
        try(InputStreamReader reader = new InputStreamReader(plugin.getResource("plugin.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
            return config.getInt("devbukkit-project") != 0;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public UpdaterFactory withFrequency(Duration updateInterval) {
        this.frequency = updateInterval;
        return this;
    }

    @Override
    public Set<Capability> capabilities(Plugin plugin) {
        return new HashSet<>(Arrays.asList(
            Capability.VERSION_CHECK,
            Capability.PLATFORM,
            Capability.WEBLINK,
            Capability.DOWNLOAD,
            Capability.COMPATIBILITY_CHECK
        ));
    }
}
