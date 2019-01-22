package github.scarsz.pluginmessageframework.gateway;

import github.scarsz.pluginmessageframework.internal.GatewaySupport;
import github.scarsz.pluginmessageframework.packet.BasePacket;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a connection on a server implementation to a proxy/player.
 *
 * @param <C> the client connection type
 */
public interface ServerGateway<C> extends Gateway<C> {

    /**
     * Sends the packet on a gateway provided by the {@link GatewaySupport} specified in the constructor.
     *
     * @param packet the packet to send
     * @return true if the packet was sent immediately, false if a gateway couldn't be found and the packet has been queued for later
     */
    boolean sendPacket(BasePacket packet);

    /**
     * Sends the packet on a gateway provided by the {@link GatewaySupport} specified in the constructor.
     *
     * @param packet the packet to send
     * @param queue if there is no available gateway, should this packet queue until a connection becomes available
     * @return true if the packet was sent immediately, false if a gateway couldn't be found and the packet has been queued for later (if queue parameter is true)
     */
    boolean sendPacket(BasePacket packet, boolean queue);

    /**
     * Asynchronously wait until a packet matching the given condition is received then call the given action
     *
     * @param <T> The packet type to wait for
     * @param packet The packet type to wait for
     * @param condition The condition that indicates whether this wait event will accept this packet
     * @param action The action that should occur when the condition is met
     */
    <T extends BasePacket> void awaitPacket(Class<T> packet, Predicate<T> condition, Consumer<T> action);

    /**
     * Asynchronously wait until a packet matching the given condition is received then call the given action. If no packet matches in time, then the timeout action will occur instead.
     *
     * @param <T> The packet type to wait for
     * @param packet The packet type to wait for
     * @param condition The condition that indicates whether this wait event will accept this packet
     * @param action The action that should occur when the condition is met
     * @param timeout The amount of time to wait until aborting and calling the timeout action
     * @param unit The unit of time that the timeout is specified in
     * @param timeoutAction The action that should occur when the condition was not met in time
     */
    <T extends BasePacket> void awaitPacket(Class<T> packet, Predicate<T> condition, Consumer<T> action, long timeout, TimeUnit unit, Runnable timeoutAction);

    /**
     * Gets a connection which can be used to send a packet.
     *
     * @return the connection (may be null)
     */
    C getConnection();

}
