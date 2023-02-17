package com.lenis0012.pluginutils.command.defaults;

import com.lenis0012.pluginutils.command.api.CommandContext;
import com.lenis0012.pluginutils.command.api.CommandException;
import com.lenis0012.pluginutils.command.api.Completion;
import com.lenis0012.pluginutils.command.api.Context;
import com.lenis0012.pluginutils.command.api.Resolver;
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

public class CommandDefaults {

    @Completion(Player.class)
    public List<String> completePlayer() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
    
    @Resolver(Player.class)
    public Player resolvePlayer(String input) {
        Player player = Bukkit.getPlayer(input);
        if(player == null) {
            throw new CommandException(DefaultMessages.INVALID_PLAYER, input);
        }
        return player;
    }

    @Context
    @Resolver(CommandSender.class)
    public CommandSender resolveSender(CommandContext context) {
        return context.getSender();
    }

    @Context
    @Resolver(Player.class)
    public Player resolvePlayerSender(CommandContext context) {
        return context.getPlayerSender();
    }
    
    @Resolver(OfflinePlayer.class)
    public OfflinePlayer resolveOfflinePlayer(String input) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
        if(offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            throw new CommandException(DefaultMessages.INVALID_OFFLINE_PLAYER, input);
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
            throw new CommandException(DefaultMessages.INVALID_MATERIAL, input);
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
            throw new CommandException(DefaultMessages.INVALID_WORLD, input);
        }
        return world;
    }

    @Resolver(Boolean.class)
    public Boolean resolveBoolean(String input) {
        return Boolean.parseBoolean(input);
    }
    
    @Resolver(String.class)
    public String resolveString(String input) {
        return input;
    }
    
    @Resolver(Integer.class)
    public Integer resolveInteger(String input) {
        try {
            return Integer.parseInt(input);
        } catch(NumberFormatException e) {
            throw new CommandException(DefaultMessages.INVALID_NUMBER, input);
        }
    }
    
    @Resolver(Double.class)
    public Double resolveDouble(String input) {
        try {
            return Double.parseDouble(input);
        } catch(NumberFormatException e) {
            throw new CommandException(DefaultMessages.INVALID_NUMBER, input);
        }
    }
}
