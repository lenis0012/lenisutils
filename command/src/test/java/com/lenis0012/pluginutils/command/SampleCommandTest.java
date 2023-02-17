package com.lenis0012.pluginutils.command;

import com.lenis0012.pluginutils.command.api.Command;
import com.lenis0012.pluginutils.command.defaults.CommandDefaults;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SampleCommandTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Server server;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Plugin plugin;

    @Captor
    private ArgumentCaptor<CommandExecutor> executorCaptor;

    @Mock
    private CommandSender sender;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private World world;

    @BeforeEach
    void setUp() {
        Bukkit.setServer(server);
        lenient().when(plugin.getServer()).thenReturn(server);
        lenient().when(server.getWorlds()).thenReturn(Arrays.asList(world));
    }

    @Test
    void test() {
        PluginCommand pluginCommandStub = server.getPluginCommand("sample");
        doNothing().when(pluginCommandStub).setExecutor(executorCaptor.capture());
        when(world.getTime()).thenReturn(10L);

        World otherWorld = mock(World.class);
        when(otherWorld.getTime()).thenReturn(20L);
        when(server.getWorld("other")).thenReturn(otherWorld);

        org.bukkit.command.Command bukkitCommand = mock(org.bukkit.command.Command.class);
        when(bukkitCommand.getName()).thenReturn("sample");

        CommandRegistry registry = new CommandRegistry(plugin);
        registry.register(new SampleCommand());
        registry.register(new CommandDefaults());
        registry.finishAndApply();

        CommandExecutor executor = executorCaptor.getValue();
        executor.onCommand(sender, bukkitCommand, "sample", new String[] { "time" });
        verify(sender).sendMessage("Current time: 10 ticks");

        executor.onCommand(sender, bukkitCommand, "sample", new String[] { "time", "other", "seconds" });
        verify(sender).sendMessage("Current time: 20 seconds");
    }

    public static class SampleCommand {

        @Command("/sample time")
        @Command("/sample time <world> <timeName>")
        public void time(CommandSender sender, World world, String timeName) {
            if (world == null) {
                world = Bukkit.getWorlds().get(0);
            }
            if(timeName == null) {
                timeName = "ticks";
            }
            sender.sendMessage("Current time: " + world.getTime() + " " + timeName);
        }
    }
}