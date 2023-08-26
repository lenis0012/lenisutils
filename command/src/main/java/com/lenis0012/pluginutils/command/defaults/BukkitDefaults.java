package com.lenis0012.pluginutils.command.defaults;

import com.lenis0012.pluginutils.command.api.CommandContext;
import com.lenis0012.pluginutils.command.api.CommandException;
import com.lenis0012.pluginutils.command.api.Completion;
import com.lenis0012.pluginutils.command.api.Context;
import com.lenis0012.pluginutils.command.api.Resolver;
import com.lenis0012.pluginutils.command.platform.BukkitCommandAuthor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BukkitDefaults {

    @Completion(Player.class)
    public List<String> completePlayer() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
    
    @Resolver(Player.class)
    public Player resolvePlayer(String input) {
        Player player = Bukkit.getPlayer(input);
        if(player == null) {
            throw new CommandException(CommandErrorMessage.INVALID_PLAYER, input);
        }
        return player;
    }

    @Context
    @Resolver(CommandSender.class)
    public CommandSender resolveSender(CommandContext context) {
        return ((BukkitCommandAuthor) context.getAuthor()).getSender();
    }

    @Context
    @Resolver(Player.class)
    public Player resolvePlayerSender(CommandContext context) {
        if(!context.getAuthor().isPlayer()) {
            throw new CommandException(CommandErrorMessage.EXECUTOR_NOT_PLAYER);
        }
        return (Player) ((BukkitCommandAuthor) context.getAuthor()).getSender();
    }
    
    @Resolver(OfflinePlayer.class)
    public OfflinePlayer resolveOfflinePlayer(String input) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
        if(offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            throw new CommandException(CommandErrorMessage.INVALID_OFFLINE_PLAYER, input);
        }
        return offlinePlayer;
    }
    
    @Completion(Material.class)
    public List<String> completeMaterial() {
        return Arrays.stream(Material.values()).map(m -> m.name().toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    }

    @Resolver(Material.class)
    public Material resolveMaterial(String input) {
        Material material = Material.matchMaterial(input.toUpperCase(Locale.ROOT));
        if(material == null) {
            throw new CommandException(CommandErrorMessage.INVALID_MATERIAL, input);
        }
        return material;
    }

    @Completion(World.class)
    public List<String> completeWorld() {
        return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
    }

    @Resolver(World.class)
    public World resolveWorld(String input) {
        World world = Bukkit.getWorld(input);
        if(world == null) {
            throw new CommandException(CommandErrorMessage.INVALID_WORLD, input);
        }
        return world;
    }
}
