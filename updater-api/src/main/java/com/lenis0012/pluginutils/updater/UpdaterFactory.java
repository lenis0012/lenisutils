package com.lenis0012.pluginutils.updater;

import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.StreamSupport;

public interface UpdaterFactory {

    Updater getUpdater(Plugin plugin);

    UpdaterFactory withDownloadPrompt(String downloadCommand);

    UpdaterFactory withChannel(UpdateChannel channel);

    UpdaterFactory withFrequency(Duration updateInterval);

    UpdaterFactory withConfiguration(Map<String, Object> configuration);

    Set<Capability> capabilities();

    static UpdaterFactory provideBest(ClassLoader loader) {
        ServiceLoader<UpdaterFactory> serviceLoader = ServiceLoader.load(UpdaterFactory.class, loader);
        return StreamSupport.stream(serviceLoader.spliterator(), false)
                .max(Comparator.comparingInt(factory -> factory.capabilities().size()))
                .orElseThrow(() -> new IllegalStateException("No updater implementation found"));
    }

    enum Capability {
        VERSION_CHECK,
        CHANNELS,
        COMPATIBILITY_CHECK,
        WEBLINK,
        DOWNLOAD,
        DEV_BUILDS
    }
}
