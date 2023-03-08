package com.lenis0012.pluginutils.command;

import com.lenis0012.pluginutils.command.api.Command;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

@Command("/sample")
public class SampleCommand {

    @Command("time")
    @Command("time <world>")
    public void time(CommandSender sender, World world) {
        sender.sendMessage("Current time: " + Bukkit.getWorlds().get(0).getTime());
    }
}
