package github.scarsz.pluginmessageframework.bungeecord;

import github.scarsz.pluginmessageframework.bungeecord.packets.PacketForwardToPlayer;
import github.scarsz.pluginmessageframework.bungeecord.packets.PacketForwardToServer;
import github.scarsz.pluginmessageframework.bungeecord.packets.PacketGetServer;
import github.scarsz.pluginmessageframework.bungeecord.packets.PacketGetServers;
import github.scarsz.pluginmessageframework.bungeecord.packets.PacketIP;
import github.scarsz.pluginmessageframework.bungeecord.packets.PacketPlayerCount;
import github.scarsz.pluginmessageframework.bungeecord.packets.PacketPlayerList;
import github.scarsz.pluginmessageframework.bungeecord.packets.PacketServerIP;
import github.scarsz.pluginmessageframework.bungeecord.packets.PacketUUID;
import github.scarsz.pluginmessageframework.gateway.payload.PayloadHandler;
import github.scarsz.pluginmessageframework.gateway.payload.basic.BasicPayloadHandler;
import github.scarsz.pluginmessageframework.packet.RawPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple factory class for getting a {@link PayloadHandler} capable of handling BungeeCord packets.
 */
public class BungeeCordHelper {

    public static final String BUNGEECORD_CHANNEL = "BungeeCord";

    @SuppressWarnings("serial")
    private static final List<Class<? extends RawPacket>> packets = new ArrayList<Class<? extends RawPacket>>() {
        {
            add(PacketForwardToPlayer.class);
            add(PacketForwardToServer.class);
            add(PacketGetServer.class);
            add(PacketGetServers.class);
            add(PacketIP.class);
            add(PacketPlayerCount.class);
            add(PacketPlayerList.class);
            add(PacketUUID.class);
            add(PacketServerIP.class);
        }
    };

    /**
     * Gets a {@link PayloadHandler} capable of handling BungeeCord packets.
     *
     * @return the payload handler
     */
    public static PayloadHandler getPayloadHandler() {
        BasicPayloadHandler basicPayloadHandler = new BasicPayloadHandler();
        basicPayloadHandler.registerAllIncomingPackets(packets);
        return basicPayloadHandler;
    }

    private BungeeCordHelper() {}

}
