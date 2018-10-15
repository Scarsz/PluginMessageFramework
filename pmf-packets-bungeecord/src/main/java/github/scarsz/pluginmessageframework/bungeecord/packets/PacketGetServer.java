package github.scarsz.pluginmessageframework.bungeecord.packets;

import github.scarsz.pluginmessageframework.gateway.payload.basic.IncomingHandler;
import github.scarsz.pluginmessageframework.packet.RawPacket;

/**
 * Get this server's name, as defined in BungeeCord's config.yml.
 */
public class PacketGetServer extends RawPacket {

    public static final String TAG = "GetServer";

    private String serverName;

    /**
     * Creates a new instance.
     */
    public PacketGetServer() {
        super(TAG);
    }

    @IncomingHandler(TAG)
    private PacketGetServer(String serverName) {
        this();
        this.serverName = serverName;
    }

    /**
     * Gets the servers name as defined in BungeeCord's config.yml.
     *
     * @return the server name
     */
    public String getServerName() {
        throwExceptionIfAttemptingReadBeforeReceived();
        return serverName;
    }

}
