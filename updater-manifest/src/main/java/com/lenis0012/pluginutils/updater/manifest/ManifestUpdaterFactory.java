package com.lenis0012.pluginutils.updater.manifest;

import com.lenis0012.pluginutils.updater.UpdateChannel;
import com.lenis0012.pluginutils.updater.Updater;
import com.lenis0012.pluginutils.updater.UpdaterFactory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ManifestUpdaterFactory implements UpdaterFactory {
    private Duration frequency = DEFAULT_FREQUENCY;
    private UpdateChannel channel = UpdateChannel.STABLE;

    @Override
    public Updater getUpdater(Plugin plugin) {
        String manifestUrl;
        try(InputStreamReader reader = new InputStreamReader(plugin.getResource("plugin.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
            manifestUrl = config.getString("manifest-url");
        } catch(Exception e) {
            throw new IllegalStateException("Failed to load plugin.yml", e);
        }
        return new ManifestUpdater(plugin, frequency, manifestUrl, channel);
    }

    @Override
    public boolean isCompatible(Plugin plugin) {
        try(InputStreamReader reader = new InputStreamReader(plugin.getResource("plugin.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
            String url = config.getString("manifest-url");
            if(url == null) {
                return false;
            }

            new URL(url);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public UpdaterFactory withChannel(UpdateChannel channel) {
        this.channel = channel;
        return this;
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
            Capability.COMPATIBILITY_CHECK,
            Capability.CHANNELS,
            Capability.WEBLINK,
            Capability.DOWNLOAD
        ));
    }
}
