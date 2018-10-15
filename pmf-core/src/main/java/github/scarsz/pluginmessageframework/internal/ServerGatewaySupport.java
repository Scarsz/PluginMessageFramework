package github.scarsz.pluginmessageframework.internal;

import github.scarsz.pluginmessageframework.gateway.ServerGateway;
import github.scarsz.pluginmessageframework.packet.BasePacket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Support class for {@link ServerGateway} implementations.
 *
 * @param <C> the client connection type
 */
public abstract class ServerGatewaySupport<C> extends GatewaySupport<C> implements ServerGateway<C> {

    private final List<BasePacket> standardPacketQueue = new ArrayList<>();

    public ServerGatewaySupport() {
        super(null);
    }

    public ServerGatewaySupport(String channel) {
        super(channel);
    }

    protected boolean queuedPackets() {
        return standardPacketQueue.size() > 0;
    }

    protected void sendQueuedPackets(C connection) {
        Iterator<BasePacket> iterator = standardPacketQueue.iterator();
        while (iterator.hasNext()) {
            BasePacket standardPacket = iterator.next();

            try {
                sendPacket(connection, standardPacket);
            } catch (Throwable e) {
                logger.error("Error sending queued packet.", e);
            }

            iterator.remove();
        }
    }

    @Override
    public boolean sendPacket(BasePacket packet) {
        return sendPacket(packet, true);
    }

    @Override
    public boolean sendPacket(BasePacket packet, boolean queue) {
        C connection = getConnection();
        if (connection == null) {
            if (queue) {
                standardPacketQueue.add(packet);
            }

            return false;
        }

        sendPacket(connection, packet);
        return true;
    }

}
