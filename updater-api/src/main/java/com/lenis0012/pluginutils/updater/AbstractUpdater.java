package com.lenis0012.pluginutils.updater;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public abstract class AbstractUpdater implements Updater {
    protected final Plugin plugin;
    protected BukkitTask task;
    private VersionNumber currentVersion;
    private Version latestVersion;

    public AbstractUpdater(Plugin plugin, Duration frequency) {
        this.plugin = plugin;
        this.currentVersion = VersionNumber.of(plugin.getDescription().getVersion());
        this.task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::check, 0L,
                frequency.getSeconds() * 20L);

        plugin.getServer().getPluginManager().registerEvents(new CommandInterceptor(), plugin);
    }

    private void check() {
        try {
            this.latestVersion = fetchLatestVersion();
        } catch(Exception e) {
            // ignore
        }
    }

    protected abstract Version fetchLatestVersion();

    @Override
    public boolean isUpdateAvailable() {
        return latestVersion != null && latestVersion.getVersionNumber().greaterThan(currentVersion);
    }

    @Override
    public Version getLatestVersion() {
        return latestVersion;
    }

    @Override
    public CompletableFuture<InstalledVersion> downloadLatestVersion() {
        if(latestVersion == null) {
            throw new IllegalStateException("No update available");
        }
        if(latestVersion.getDownloadUrl() == null) {
            CompletableFuture<InstalledVersion> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("No download url available"));
            return future;
        }

        return CompletableFuture.supplyAsync(() -> {
            URL url;
            InputStream input = null;
            FileOutputStream output = null;
            try {
                url = new URL(latestVersion.getDownloadUrl());
                input = url.openStream();

                Method getFileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
                getFileMethod.setAccessible(true);
                File file = (File) getFileMethod.invoke(plugin);
                output = new FileOutputStream(new File(Bukkit.getUpdateFolderFile(), file.getName()));
                byte[] buffer = new byte[1024];
                int length;
                while((length = input.read(buffer, 0, buffer.length)) != -1) {
                    output.write(buffer, 0, length);
                }
                plugin.getLogger().log(Level.INFO, "Download complete!");
                return new InstalledVersion(latestVersion);
            } catch (Exception e) {
                throw new RuntimeException("Failed to download update", e);
            } finally {
                if(input != null) {
                    try {
                        input.close();
                    } catch(IOException e) {
                        // ignore
                    }
                }
                if(output != null) {
                    try {
                        output.close();
                    }catch(IOException e) {
                        // ignore
                    }
                }
            }
        }, task -> Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public void notifyIfUpdateAvailable(Player player) {
        if(latestVersion == null || latestVersion.getVersionNumber().lessThanOrEqual(currentVersion)) {
            return;
        }
        if(VersionNumber.ofBukkit().greaterThanOrEqual(VersionNumber.of("1.16"))) {
            if(player.getPersistentDataContainer().has(new NamespacedKey(plugin, "ignored-update"), PersistentDataType.STRING)) {
                String ignoredVersion = player.getPersistentDataContainer().get(new NamespacedKey(plugin, "ignored-update"), PersistentDataType.STRING);
                if(VersionNumber.of(ignoredVersion).greaterThanOrEqual(latestVersion.getVersionNumber())) {
                    return;
                }
            }
        }

        player.spigot().sendMessage(new ComponentBuilder("A new version of ").retain(ComponentBuilder.FormatRetention.FORMATTING).color(ChatColor.GREEN)
            .append(plugin.getName())
            .append(" is available: ")
            .append("v" + latestVersion.getVersionNumber().toString()).color(ChatColor.DARK_GREEN)
            .append(" (you are running ").color(ChatColor.GREEN)
            .append("v" + currentVersion.toString()).color(ChatColor.DARK_GREEN)
            .append(")").color(ChatColor.GREEN)
            .create()
        );

        if (latestVersion.getMinMinecraftVersion() != null) {
            ComponentBuilder builder = new ComponentBuilder("This version is made for Minecraft ").color(ChatColor.GREEN);
            builder.append(latestVersion.getMinMinecraftVersion().toString()).color(ChatColor.DARK_GREEN);
            if(latestVersion.getMaxMinecraftVersion() != null) {
                builder.append(" - " + latestVersion.getMaxMinecraftVersion()).color(ChatColor.DARK_GREEN);
            } else {
                builder.append(" and above");
            }
            player.spigot().sendMessage(builder.create());
        }

        ComponentBuilder footer = new ComponentBuilder("    ");
        if(latestVersion.getDownloadUrl() != null) {
            footer.append("[Download]").color(ChatColor.GREEN).bold(true)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to download the update").create()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.getName() + ":updater download")).append("  ");
        }
        footer.append("[Changelog]").color(ChatColor.GREEN).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to view the changelog").create()))
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, latestVersion.getChangelogUrl())).append("  ");
        if (VersionNumber.ofBukkit().greaterThanOrEqual(VersionNumber.of("1.16"))) {
            footer.append("Dismiss").color(ChatColor.GRAY).bold(true)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to dismiss this message").create()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.getName() + ":updater dismiss"));
        }
        player.spigot().sendMessage(footer.create());
    }

    class CommandInterceptor implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onCommand(PlayerCommandPreprocessEvent event) {
            final Player player = event.getPlayer();
            if(event.getMessage().equals("/" + plugin.getName() + ":updater download")) {
                event.setCancelled(true);
                if(latestVersion == null) {
                    return;
                }

                player.sendMessage(ChatColor.GREEN + "Downloading update...");
                downloadLatestVersion().thenAccept(version -> {
                    player.sendMessage(ChatColor.GREEN + "Update downloaded!");
                    player.sendMessage(ChatColor.GREEN + "Restart your server to apply the update.");
                }).exceptionally(e -> {
                    plugin.getLogger().log(Level.WARNING, "Failed to download update", e);
                    player.sendMessage(ChatColor.RED + "Failed to download update! Please download it manually from " + latestVersion.getDownloadUrl());
                    return null;
                });
            } else if(event.getMessage().equals("/" + plugin.getName() + ":updater dismiss")) {
                event.setCancelled(true);
                player.getPersistentDataContainer().set(NamespacedKey.fromString("ignored-update", plugin), PersistentDataType.STRING, latestVersion.getVersionNumber().toString());
            }
        }
    }
}
