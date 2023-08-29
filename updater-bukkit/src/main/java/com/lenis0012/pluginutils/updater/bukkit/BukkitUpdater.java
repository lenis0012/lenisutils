package com.lenis0012.pluginutils.updater.bukkit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lenis0012.pluginutils.updater.AbstractUpdater;
import com.lenis0012.pluginutils.updater.UpdateChannel;
import com.lenis0012.pluginutils.updater.Version;
import com.lenis0012.pluginutils.updater.VersionNumber;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BukkitUpdater extends AbstractUpdater {
    private static final Pattern VERSION_TITLE_PATTERN = Pattern.compile("\\(MC [^)]+\\)");
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+(\\.\\d+)+");
    private static final Pattern FILE_URL_PATTERN = Pattern.compile("https://www\\.curseforge\\.com/minecraft/bukkit-plugins/loginsecurity/download/(\\d+)");

    private final Plugin plugin;
    private final JsonParser jsonParser = new JsonParser();
    private final int projectId;

    public BukkitUpdater(Plugin plugin, Duration frequency) {
        super(plugin, frequency);
        this.plugin = plugin;

        try(InputStreamReader reader = new InputStreamReader(plugin.getResource("plugin.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
            this.projectId = config.getInt("devbukkit-project");
        } catch(Exception e) {
            throw new IllegalArgumentException("Plugin dev.bukkit information could not be parsed", e);
        }
    }

    @Override
    protected Version fetchLatestVersion() {
        JsonElement json;
        try {
            json = readJsonFromURL("https://api.curseforge.com/servermods/files?projectIds=" + projectId);
        } catch (IOException e) {
            verboseLog("Failed to read from CurseForge API", e);
            return null;
        }

        // Try to parse all versions
        JsonArray files = json.getAsJsonArray();
        Stream<Version> versionStream = IntStream.range(0, files.size()).mapToObj(i -> {
            JsonObject file = files.get(i).getAsJsonObject();
            try {
                return parseVersion(file);
            } catch (Exception e) {
                verboseLog("Failed to parse version", e);
                return null;
            }
        }).filter(Objects::nonNull);

        // Find latest compatible version
        return versionStream
            .filter(version -> isCompatible(version, UpdateChannel.STABLE))
            .max(Comparator.comparing(Version::getVersionNumber, VersionNumber.comparator()))
            .orElse(null);
    }

    private Version parseVersion(JsonObject info) {
        String name = info.get("name").getAsString();
        String serverVersion = info.get("gameVersion").getAsString();
        String downloadURL = info.get("downloadUrl").getAsString();

        VersionNumber minimumVersion = VersionNumber.of(serverVersion);
        Matcher titleMatcher = VERSION_TITLE_PATTERN.matcher(name);
        if(titleMatcher.find()) {
            String title = titleMatcher.group();
            Matcher versionMatcher = VERSION_PATTERN.matcher(title);
            while(versionMatcher.find()) {
                String version = versionMatcher.group();
                VersionNumber versionNumber = VersionNumber.of(version);
                if(versionNumber.lessThan(minimumVersion)) {
                    minimumVersion = versionNumber;
                }
            }
        }

        String changelogURL = null;
        Matcher fileURLMatcher = FILE_URL_PATTERN.matcher(info.get("fileUrl").getAsString());
        if(fileURLMatcher.find()) {
            String fileId = fileURLMatcher.group(1);
            changelogURL = "https://dev.bukkit.org/projects/loginsecurity/files/" + fileId;
        }

        return Version.builder()
            .versionNumber(VersionNumber.of(name))
            .minMinecraftVersion(minimumVersion)
            .downloadUrl(downloadURL)
            .changelogUrl(changelogURL)
            .build();
    }

    private JsonElement readJsonFromURL(String downloadURL) throws IOException {
        URL url = new URL(downloadURL);
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        connection.addRequestProperty("User-Agent", "lenisutils/v3 (by " + plugin.getName() + ")");
        connection.addRequestProperty("Accept", "application/json");

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return jsonParser.parse(builder.toString());
        }
    }
}
