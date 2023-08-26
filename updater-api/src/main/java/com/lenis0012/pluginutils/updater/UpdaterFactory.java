package com.lenis0012.pluginutils.updater;

import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.StreamSupport;

public interface UpdaterFactory {
    Duration DEFAULT_FREQUENCY = Duration.ofHours(3);

    Updater getUpdater(Plugin plugin);

    boolean isCompatible(Plugin plugin);

    UpdaterFactory withFrequency(Duration updateInterval);

    default UpdaterFactory withChannel(UpdateChannel channel) {
        return this;
    }

    Set<Capability> capabilities(Plugin plugin);

    static UpdaterFactory provideBest(Plugin plugin, ClassLoader loader) {
        ToIntFunction<UpdaterFactory> toSupportLevel = factory ->
            Arrays.stream(Capability.values())
                // Find first missing capability
                .filter(capability -> !factory.capabilities(plugin).contains(capability))
                .mapToInt(Capability::ordinal)
                .findFirst()
                // All capabilities supported
                .orElse(Capability.values().length);

        ServiceLoader<UpdaterFactory> serviceLoader = ServiceLoader.load(UpdaterFactory.class, loader);
        return StreamSupport.stream(serviceLoader.spliterator(), false)
            .sorted(Comparator.comparingInt(toSupportLevel))
            .filter(factory -> factory.isCompatible(plugin))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No updater implementation present. Service files missing?"));
    }

    enum Capability {
        /**
         * Capable of retrieving the latest version(s) of a plugin.
         */
        VERSION_CHECK,
        /**
         * Platform implementation.
         * A platform is referring to an official source where the plugin is hosted.
         */
        PLATFORM,
        /**
         * Capable of checking version compatability against the current server version.
         * This also implies support for multiple versions to be checked so the most recent compatible version can be picked.
         */
        COMPATIBILITY_CHECK,
        CHANNELS,
        WEBLINK,
        DOWNLOAD,
        DEV_BUILDS
    }
}
