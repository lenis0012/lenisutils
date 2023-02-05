package com.lenis0012.pluginutils.sql;

import ch.vorburger.mariadb4j.DB;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Ignore
class ConnectionPoolDataSourceTest {
    private static DB db;

    @BeforeAll
    static void prepareDB() throws Exception {
        db = DB.newEmbeddedDB(3306);
        db.start();
        db.source("seed.sql", "test");
    }

    @AfterAll
    static void shutdownDB() throws Exception {
        db.stop();
    }

    private Plugin plugin;
    private Server server;
    private BukkitScheduler scheduler;

    @BeforeEach
    void prepareMocks() {
        this.plugin = Mockito.mock(Plugin.class);
        this.server = Mockito.mock(Server.class);
        this.scheduler = Mockito.mock(BukkitScheduler.class);
        BukkitTask task = Mockito.mock(BukkitTask.class);
        when(plugin.getServer()).thenReturn(server);
        when(plugin.getLogger()).thenReturn(Logger.getLogger(getClass().getName()));
        when(server.getScheduler()).thenReturn(scheduler);
        when(scheduler.runTaskTimerAsynchronously(eq(plugin), any(Runnable.class), anyLong(), anyLong())).thenReturn(task);
    }

    @Test
    void testSimpleQuery() throws Exception {
        ConnectionPoolDataSource dataSource = (ConnectionPoolDataSource) DataSourceBuilder.mysqlBuilder(plugin)
                .database("test")
                .build();

        try(Connection connection = dataSource.getConnection()) {
            ResultSet result = connection.createStatement().executeQuery("SELECT 1");
            result.next();
            assertThat(result.getInt(1)).isEqualTo(1);
        }
    }

    @Test
    void testParallelism() throws Exception {
        ConnectionPoolDataSource dataSource = (ConnectionPoolDataSource) DataSourceBuilder.mysqlBuilder(plugin)
                .database("test")
                .build();

        AtomicInteger count = new AtomicInteger();

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 10000; i++) {
            executorService.execute(() -> {
                try(Connection connection = dataSource.getConnection()) {
                    ResultSet result = connection.createStatement().executeQuery("SELECT title FROM tasks WHERE id=1;");
                    result.next();
                    assertThat(result.getString(1)).isEqualTo("Task 1");
                    count.incrementAndGet();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
        try {
            dataSource.close();
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }
        assertThat(count.get()).isEqualTo(10000);
    }

    @Test
    void testRecoverOnRestart() throws Exception {
        ConnectionPoolDataSource dataSource = (ConnectionPoolDataSource) DataSourceBuilder.mysqlBuilder(plugin)
                .database("test")
                .validationBypassThreshold(0)
                .maxPoolSize(1)
                .build();

        try(Connection connection = dataSource.getConnection()) {
            ResultSet result = connection.createStatement().executeQuery("SELECT 1");
            result.next();
            assertThat(result.getInt(1)).isEqualTo(1);
        }

        db.stop();
        db.start();

        try(Connection connection = dataSource.getConnection()) {
            ResultSet result = connection.createStatement().executeQuery("SELECT 1");
            result.next();
            assertThat(result.getInt(1)).isEqualTo(1);
        }
    }
}