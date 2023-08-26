package com.lenis0012.pluginutils.updater.manifest;

import com.google.gson.Gson;
import com.lenis0012.pluginutils.updater.UpdateChannel;
import com.lenis0012.pluginutils.updater.Version;
import com.lenis0012.pluginutils.updater.VersionNumber;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManifestUpdaterTest {
    private static final Gson GSON = new Gson();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Plugin plugin;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Server server;

    @Captor
    private ArgumentCaptor<Listener> listenerCaptor;

    @Captor
    private ArgumentCaptor<Runnable> versionCheckCaptor;

    private static HttpServer httpServer;
    private static HttpHandler httpHandler;

    @BeforeAll
    static void setUpServer() throws Exception {
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(0), 0);
        httpServer.createContext("/manifest.json", httpHandler = mock(HttpHandler.class));
        httpServer.start();
    }

    @AfterAll
    static void tearDownServer() {
        httpServer.stop(0);
    }

    @BeforeEach
    void setUp() throws Exception {
        setServer(server);
        when(plugin.getServer()).thenReturn(server);
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        doNothing().when(pluginManager)
            .registerEvents(listenerCaptor.capture(), eq(plugin));

        when(plugin.getServer().getScheduler().runTaskTimerAsynchronously(eq(plugin), versionCheckCaptor.capture(), anyLong(), anyLong()))
            .thenReturn(mock(BukkitTask.class));

        // Logging
        Logger logger = plugin.getLogger();
        lenient().doAnswer(invocation -> {
            System.out.println("[LOG] " + invocation.getArgument(1));
            return null;
        }).when(logger).log(any(Level.class), anyString());
        lenient().doAnswer(invocation -> {
            System.err.println("[LOG] " + invocation.getArgument(1));
            ((Throwable) invocation.getArgument(2)).printStackTrace();
            return null;
        }).when(logger).log(any(Level.class), anyString(), any(Throwable.class));
    }

    @Test
    void testVersionFetch() {
        Manifest manifest = Manifest.builder()
            .version("1.0.1")
            .build();
        ManifestUpdater updater = givenManifestUpdater(manifest, "1.0.0-SNAPSHOT", "1.16.5-R0.1-SNAPSHOT");

        // When
        Version result = updater.fetchLatestVersion();

        // Then
        assertNotNull(result, "Result must be present (received valid json array)");
        assertEquals(result.getVersionNumber(), VersionNumber.of("1.0.1"));
    }

    @Test
    void testUpdateAvailable() {
        Manifest manifest = Manifest.builder()
            .version("1.0.1")
            .build();
        ManifestUpdater updater = givenManifestUpdater(manifest, "1.0.0-SNAPSHOT", "1.16.5-R0.1-SNAPSHOT");

        // When
        versionCheckCaptor.getValue().run();

        // Then
        Version result = updater.getLatestVersion();
        assertNotNull(result);
        assertEquals(result.getVersionNumber(), VersionNumber.of("1.0.1"));
        assertTrue(updater.isUpdateAvailable());
    }

    @Test
    void testNoUpdateAvailable() {
        Manifest manifest = Manifest.builder()
            .version("1.0.0")
            .build();
        ManifestUpdater updater = givenManifestUpdater(manifest, "1.0.0-SNAPSHOT", "1.16.5-R0.1-SNAPSHOT");

        // When
        versionCheckCaptor.getValue().run();

        // Then
        Version result = updater.getLatestVersion();
        assertNotNull(result);
        assertEquals(result.getVersionNumber(), VersionNumber.of("1.0.0"));
        assertFalse(updater.isUpdateAvailable());
    }

    @SneakyThrows
    private ManifestUpdater givenManifestUpdater(Manifest manifest, String pluginVersion, String bukkitVersion){
        doAnswer(invocation -> {
            HttpExchange exchange = invocation.getArgument(0);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(
                GSON.toJson(new Manifest[] { manifest }).getBytes(StandardCharsets.UTF_8)
            );
            exchange.close();
            return null;
        }).when(httpHandler).handle(any(HttpExchange.class));

        when(server.getBukkitVersion()).thenReturn(bukkitVersion);
        when(plugin.getDescription().getVersion()).thenReturn("1.0.0-SNAPSHOT");
        ManifestUpdater updater = new ManifestUpdater(plugin, Duration.ofMinutes(1), "http://localhost:" + httpServer.getAddress().getPort() + "/manifest.json", UpdateChannel.STABLE);
        return updater;
    }

    @Value
    @Builder
    static class Manifest {
        String version;
        String downloadUrl;
        String changelogUrl;
        String channel;
        String minMcVersion;
        String maxMcVersion;
    }

    @SneakyThrows
    private static void setServer(Server server) {
        Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, server);
    }
}