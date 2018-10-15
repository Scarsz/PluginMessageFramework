package github.scarsz.pluginmessageframework.internal;

import github.scarsz.pluginmessageframework.gateway.ProxyGateway;
import github.scarsz.pluginmessageframework.gateway.ProxySide;
import github.scarsz.pluginmessageframework.packet.BasePacket;

/**
 * Support class for {@link ProxyGateway} implementations.
 *
 * @param <C> the client connection type
 * @param <S> the server connection type
 */
public abstract class ProxyGatewaySupport<C, S, Q> extends GatewaySupport<C> implements ProxyGateway<C, S, Q> {

    private ProxySide proxySide;

    public ProxyGatewaySupport(ProxySide proxySide) {
        super(null);
        setProxySide(proxySide);
    }

    public ProxyGatewaySupport(String channel, ProxySide proxySide) {
        super(channel);
        setProxySide(proxySide);
    }

    private void setProxySide(ProxySide proxySide) {
        if (proxySide == null) {
            throw new IllegalArgumentException("ProxySide cannot be null.");
        }

        this.proxySide = proxySide;
    }

    @Override
    public ProxySide getProxySide() {
        return proxySide;
    }

    @Override
    public void sendPacketServer(S serverConnection, BasePacket packet) {
        sendCustomPayloadServer(serverConnection, writePacket(packet));
    }

    @Override
    public boolean sendPacketServer(Q queueableConnection, BasePacket packet, boolean queue) {
        return sendCustomPayloadServer(queueableConnection, writePacket(packet), queue);
    }

    public abstract void sendCustomPayloadServer(S serverConnection, byte[] bytes);

    public abstract boolean sendCustomPayloadServer(Q queueableConnection, byte[] bytes, boolean queue);

}
