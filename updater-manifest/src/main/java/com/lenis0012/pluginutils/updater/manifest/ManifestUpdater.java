package com.lenis0012.pluginutils.updater.manifest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lenis0012.pluginutils.updater.AbstractUpdater;
import com.lenis0012.pluginutils.updater.UpdateChannel;
import com.lenis0012.pluginutils.updater.Version;
import com.lenis0012.pluginutils.updater.VersionNumber;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.logging.Level;

public class ManifestUpdater extends AbstractUpdater {
    private final JsonParser jsonParser = new JsonParser();
    private final String manifestUrl;
    private final UpdateChannel channel;

    public ManifestUpdater(Plugin plugin, Duration frequency, String manifestUrl, UpdateChannel channel) {
        super(plugin, frequency);
        this.manifestUrl = manifestUrl;
        this.channel = channel;
    }

    @Override
    protected Version fetchLatestVersion() {
        JsonElement json = readJsonFromURL(manifestUrl);
        if(json == null) {
            // Not present (or other error)
            return null;
        }

        if(!json.isJsonArray()) {
            return null;
        }

        for(JsonElement versionElement : json.getAsJsonArray()) {
            Version version = parseVersion(versionElement.getAsJsonObject());
            if(!isCompatible(version)) {
                continue;
            }
            return version;
        }
        return null;
    }

    private boolean isCompatible(Version version) {
        VersionNumber bukkitVersion = VersionNumber.ofBukkit();
        if (version.getMinMinecraftVersion() != null && version.getMinMinecraftVersion().greaterThan(bukkitVersion)) {
            return false;
        }
        if (version.getMaxMinecraftVersion() != null && version.getMaxMinecraftVersion().lessThan(bukkitVersion)) {
            return false;
        }
        if(version.getChannel() != null && version.getChannel().ordinal() > channel.ordinal()) {
            return false;
        }
        return true;
    }

    private Version parseVersion(JsonObject info) {
        return Version.builder()
            .versionNumber(VersionNumber.of(info.get("version").getAsString()))
            .downloadUrl(info.has("downloadUrl") ? info.get("downloadUrl").getAsString() : null)
            .changelogUrl(info.has("changelogUrl") ?  info.get("changelogUrl").getAsString() : null)
            .channel(info.has("channel") ? UpdateChannel.valueOf(info.get("channel").getAsString()) : UpdateChannel.STABLE)
            .minMinecraftVersion(info.has("minMcVersion") ? VersionNumber.of(info.get("minMcVersion").getAsString()) : null)
            .maxMinecraftVersion(info.has("maxMcVersion") ? VersionNumber.of(info.get("maxMcVersion").getAsString()) : null)
            .build();
    }

    protected JsonElement readJsonFromURL(String downloadURL) {
        BufferedReader reader = null;
        try {
            URL url = new URL(downloadURL);
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            connection.addRequestProperty("User-Agent", getClass().getSimpleName() + "/v1 (by lenis0012)");

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }

            return jsonParser.parse(builder.toString());
        } catch(IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to fetch update from url " + downloadURL);
            return null;
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch(IOException e) {}
            }
        }
    }
}
