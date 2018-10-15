package github.scarsz.pluginmessageframework.internal;

import github.scarsz.pluginmessageframework.gateway.ClientGateway;
import github.scarsz.pluginmessageframework.packet.BasePacket;

/**
 * Support class for {@link ClientGateway} implementations.
 */
public abstract class ClientGatewaySupport<C> extends GatewaySupport<C> implements ClientGateway<C> {

    protected final C connection;

    public ClientGatewaySupport(String channel, C connection) {
        super(channel);
        this.connection = connection;
    }

    @Override
    public void sendPacket(BasePacket packet) {
        sendPacket(this.connection, packet);
    }

}
