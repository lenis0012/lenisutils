package com.lenis0012.pluginutils.packet;

import com.google.common.collect.Maps;
import com.lenis0012.pluginutils.modules.ModularPlugin;
import com.lenis0012.pluginutils.modules.Module;
import com.lenis0012.pluginutils.modules.misc.Reflection;
import com.lenis0012.pluginutils.modules.misc.Reflection.ClassReflection;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class PacketModule extends Module<ModularPlugin> {
    private Method GET_HANDLE;
    private Field PLAYER_CONNECTION;
    private Method SEND_PACKET;
    private final Map<String, ClassReflection> packetReflectionMap = Maps.newConcurrentMap();

    public PacketModule(ModularPlugin plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        GET_HANDLE = Reflection.getCBMethod("entity.CraftPlayer", "getHandle");
        PLAYER_CONNECTION = Reflection.getNMSField("EntityPlayer", "playerConnection");
        SEND_PACKET = Reflection.getNMSMethod("PlayerConnection", "sendPacket", Reflection.getNMSClass("Packet"));
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
