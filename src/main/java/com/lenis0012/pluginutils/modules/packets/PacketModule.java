package com.lenis0012.pluginutils.modules.packets;

import com.google.common.collect.Maps;
import com.lenis0012.pluginutils.Module;
import com.lenis0012.pluginutils.PluginHolder;
import com.lenis0012.pluginutils.misc.Reflection;
import com.lenis0012.pluginutils.misc.Reflection.ClassReflection;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class PacketModule extends Module {
    private final Method GET_HANDLE = Reflection.getCBMethod("entity.CraftPlayer", "getHandle");
    private final Field PLAYER_CONNECTION = Reflection.getNMSField("EntityPlayer", "playerConnection");
    private final Method SEND_PACKET = Reflection.getNMSMethod("PlayerConnection", "sendPacket", Reflection.getNMSClass("Packet"));
    private final Map<String, ClassReflection> packetReflectionMap = Maps.newConcurrentMap();

    public PacketModule(PluginHolder plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
    }

    /**
     * Create a new empty packet by name.
     *
     * @param name of packet
     * @return Packet
     */
    public Packet createPacket(String name) {
        ClassReflection reflection = packetReflectionMap.get(name);
        if(reflection == null) {
            reflection = new ClassReflection(Reflection.getNMSClass(name));
            packetReflectionMap.put(name, reflection);
        }

        Object instance = reflection.newInstance();
        return new Packet(reflection, instance);
    }

    /**
     * Send packet to player
     *
     * @param player to send to
     * @param packet to send
     */
    public void sendPacket(Player player, Packet packet) {
        Object entityPlayer = Reflection.invokeMethod(GET_HANDLE, player);
        Object playerConnection = Reflection.getFieldValue(PLAYER_CONNECTION, entityPlayer);
        Reflection.invokeMethod(SEND_PACKET, playerConnection, packet.getHandle());
    }

    /**
     * Broadcast packet to every player in world.
     *
     * @param world to broadcast to
     * @param packet to send
     */
    public void broadcastPacket(World world, Packet packet) {
        for(Player player : world.getPlayers()) {
            sendPacket(player, packet);
        }
    }
}
