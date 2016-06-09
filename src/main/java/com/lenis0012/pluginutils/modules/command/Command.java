package com.lenis0012.pluginutils.modules.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public abstract class Command implements CommandExecutor {
    private String usage = null;
    private String permission = null;
    private boolean allowConsole = true;
    private int minArgs = 0;

    // Format
    private String prefix = "";
    private ChatColor successColor = ChatColor.GREEN;
    private ChatColor errorColor = ChatColor.RED;

    // Info
    protected CommandSender sender;
    protected Player player;
    protected boolean isPlayer;
    private String[] args;

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        this.isPlayer = sender instanceof Player;
        this.sender = sender;
        this.args = args;
        if(!allowConsole && !isPlayer) {
            reply(false, "You must be a player to execute this command!");
            return true;
        }

        if(isPlayer) this.player = (Player) sender;
        if(permission != null && !sender.hasPermission(permission)) {
            reply(false, "You don't have permission to execute this command!");
            return true;
        }

        if(args.length < minArgs) {
            reply(false, "This command needs at least %s arguments!", minArgs);
            return true;
        }

        try {
            execute();
        } catch(Exception e) {
            reply(false, "An error occured while executing this command, please contact an admin!");
            Bukkit.getLogger().log(Level.SEVERE, "Error while running command", e);
        }
        return true;
    }

    public abstract void execute();

    protected void reply(String message, Object... args) {
        reply(true, message, args);
    }

    protected void reply(boolean success, Object message, Object... args) {
        reply(sender, success, message, args);
    }

    protected void reply(CommandSender sender, boolean success, Object message, Object... args) {
        String text = prefix + (success ? successColor : errorColor).toString() +
                ChatColor.translateAlternateColorCodes('&', String.format(message.toString(), args));
        sender.sendMessage(text);
    }

    protected String getArg(int index) {
        return args[index];
    }

    protected int getArgAsInt(int index) {
        return Integer.parseInt(getArg(index));
    }

    protected Player getArgAsPlayer(int index) {
        return Bukkit.getPlayer(getArg(index));
    }

    protected int getArgLength() {
        return args.length;
    }

    protected String getUsage() {
        return usage;
    }

    protected void setUsage(String usage) {
        this.usage = usage;
    }

    protected String getPermission() {
        return permission;
    }

    protected void setPermission(String permission) {
        this.permission = permission;
    }

    protected int getMinArgs() {
        return minArgs;
    }

    protected void setMinArgs(int minArgs) {
        this.minArgs = minArgs;
    }

    protected boolean isAllowConsole() {
        return allowConsole;
    }

    protected void setAllowConsole(boolean allowConsole) {
        this.allowConsole = allowConsole;
    }

    protected String getPrefix() {
        return prefix;
    }

    protected void setPrefix(String prefix) {
        this.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
    }

    protected ChatColor getSuccessColor() {
        return successColor;
    }

    protected void setSuccessColor(ChatColor successColor) {
        this.successColor = successColor;
    }

    protected ChatColor getErrorColor() {
        return errorColor;
    }

    protected void setErrorColor(ChatColor errorColor) {
        this.errorColor = errorColor;
    }
}
