package github.scarsz.pluginmessageframework.bungeecord.packets;

import github.scarsz.pluginmessageframework.gateway.payload.basic.IncomingHandler;
import github.scarsz.pluginmessageframework.packet.RawPacket;

/**
 * Get a list of server name strings, as defined in BungeeCord's config.yml.
 */
public class PacketGetServers extends RawPacket {

    public static final String TAG = "GetServers";

    private String[] servers;

    /**
     * Creates a new instance.
     */
    public PacketGetServers() {
        super(TAG);
    }

    @IncomingHandler(TAG)
    private PacketGetServers(String servers) {
        this();
        this.servers = servers.split(", ");
    }

    /**
     * Gets all the server name strings, as defined in BungeeCord's config.yml.
     *
     * @return the server name strings
     */
    public String[] getServers() {
        throwExceptionIfAttemptingReadBeforeReceived();
        return servers;
    }

}
