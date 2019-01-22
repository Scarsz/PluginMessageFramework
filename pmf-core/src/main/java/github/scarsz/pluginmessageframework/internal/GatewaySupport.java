package github.scarsz.pluginmessageframework.internal;

import github.scarsz.pluginmessageframework.PrimaryArgumentProvider;
import github.scarsz.pluginmessageframework.Utilities;
import github.scarsz.pluginmessageframework.gateway.Gateway;
import github.scarsz.pluginmessageframework.gateway.WaitingPacket;
import github.scarsz.pluginmessageframework.gateway.payload.PayloadHandler;
import github.scarsz.pluginmessageframework.gateway.payload.StandardPayloadHandler;
import github.scarsz.pluginmessageframework.packet.BasePacket;
import github.scarsz.pluginmessageframework.packet.PacketHandler;
import github.scarsz.pluginmessageframework.packet.PrimaryValuePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Support class for {@link Gateway} implementations.
 * Provides connections and forwards received packets to the framework.
 *
 * @param <C> the client connection type
 */
public abstract class GatewaySupport<C> implements Gateway<C> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private String channel;
    private PayloadHandler payloadHandler = null;
    private final Map<Class<?>, Set<WaitingPacket>> waitingPackets = new HashMap<>();
    private final ScheduledExecutorService waitingScheduler = Executors.newSingleThreadScheduledExecutor();

    private final Map<Class<? extends BasePacket>, List<Object>> listeners = new HashMap<>();

    public GatewaySupport(String channel) {
        if (channel == null || channel.isEmpty()) {
            throw new IllegalArgumentException("Channel cannot be null or an empty string.");
        }

        this.channel = channel;
    }

    @Override
    public final String getChannel() {
        return channel;
    }

    @Override
    public final PayloadHandler getPayloadHandler() {
        if (payloadHandler == null) {
            payloadHandler = new StandardPayloadHandler();
        }

        return payloadHandler;
    }

    @Override
    public void setPayloadHandler(PayloadHandler payloadHandler) {
        this.payloadHandler = payloadHandler;
    }

    /**
     * Helper method, checks if packet is applicable (if not an exception is thrown) and then returns the packet in a byte[] form.
     *
     * @param packet the packet to write bytes for
     * @return the byte[] representation of the packet
     */
    @SuppressWarnings("unchecked")
    public byte[] writePacket(BasePacket packet) {
        if (!getPayloadHandler().isPacketApplicable(packet)) {
            throw new IllegalArgumentException("Assigned PayloadHandler cannot handle this type of Packet.");
        }

        try {
            return getPayloadHandler().writeOutgoingPacket(packet);
        } catch (IOException e) {
            Utilities.sneakyThrow(e);
            return null; // this line is never executed
        }
    }

    @Override
    public void sendPacket(C connection, BasePacket packet) {
        sendPayload(connection, writePacket(packet));
    }

    public abstract void sendPayload(C connection, byte[] bytes);

    protected Object handleListenerParameter(Class<?> clazz, BasePacket packet, C connection) {
        // todo do this better? gets overridden
        if (packet instanceof PrimaryArgumentProvider) {
            Object object = ((PrimaryArgumentProvider) packet).getValue();

            if (clazz.isAssignableFrom(object.getClass())) {
                return object;
            }
        }

        if (clazz.isAssignableFrom(packet.getClass())) {
            return packet;
        }

        Class<?> connectionClass = connection.getClass();
        if (clazz.isAssignableFrom(connectionClass)) {
            return connectionClass.cast(connection);
        }

        if (clazz.isAssignableFrom(getClass())) {
            return this;
        }

        return null;
    }

    public void incomingPayload(C connection, byte[] data) throws IOException {
        BasePacket packet = getPayloadHandler().readIncomingPacket(data);
        if (packet != null) {
            receivePacket(connection, packet);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerListener(Object listener) {
        for (Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(PacketHandler.class)) { // todo check parameters too
                Class<?>[] parameterTypes = method.getParameterTypes();
                Class<? extends BasePacket> packetClazz = PrimaryValuePacket.class;

                for (Class<?> parameterType : parameterTypes) { // find packet class
                    if (BasePacket.class.isAssignableFrom(parameterType)) {
                        packetClazz = (Class<? extends BasePacket>) parameterType;
                        break;
                    }
                }

                List<Object> list = listeners.computeIfAbsent(packetClazz, k -> new ArrayList<>());
                list.add(listener);
            }
        }
    }

    @Override
    public void unregisterListener(Object listener) {
        for (List<Object> list : listeners.values()) {
            list.remove(listener);
        }
    }

    @Override
    public void receivePacket(C connection, BasePacket packet) {
        Class<? extends BasePacket> packetClass = packet.getClass();

        if (waitingPackets.containsKey(packetClass)) {
            // there are packet listeners waiting for this event
            Set<WaitingPacket> set = this.waitingPackets.get(packetClass);
            // make all waiting packets attempt their conditions with this received packet
            // if the condition passes, the action is executed right now
            Set<WaitingPacket> successful = set.stream().filter(waitingPacket -> waitingPacket.attempt(packet)).collect(Collectors.toSet());
            // get rid of the packets that were successful from the waiting lists
            set.removeAll(successful);
        }

        if (listeners.containsKey(packetClass)) {
            for (Object listener : listeners.get(packetClass)) {
                methodLoop: for (Method method : listener.getClass().getMethods()) {
                    if (method.isAnnotationPresent(PacketHandler.class)) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Object[] parameters = new Object[parameterTypes.length];

                        for (int i = 0; i < parameters.length; i++) {
                            Class<?> parameterType = parameterTypes[i];
                            Object parameter = handleListenerParameter(parameterType, packet, connection);

                            if (parameter != null) {
                                parameters[i] = parameter;
                            } else {
                                continue methodLoop;
                            }
                        }

                        try {
                            method.invoke(listener, parameters);
                        } catch (IllegalAccessException e) {
                            logger.error("Error occurred whilst dispatching packet to listeners.", e);
                        } catch (InvocationTargetException e) {
                            Throwable throwable = e.getCause();
                            if (throwable == null) {
                                throwable = e;
                            }

                            Utilities.sneakyThrow(throwable);
                        }
                    }
                }
            }
        }
    }

    /**
     * Asynchronously wait until a packet matching the given condition is received then call the given action
     *
     * @param <T> The packet type to wait for
     * @param packet The packet type to wait for
     * @param condition The condition that indicates whether this wait event will accept this packet
     * @param action The action that should occur when the condition is met
     */
    public <T extends BasePacket> void awaitPacket(Class<T> packet, Predicate<T> condition, Consumer<T> action) {
        awaitPacket(packet, condition, action, -1, null, null);
    }

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
    public <T extends BasePacket> void awaitPacket(Class<T> packet, Predicate<T> condition, Consumer<T> action,
                                                   long timeout, TimeUnit unit, Runnable timeoutAction) {
        Objects.requireNonNull(packet, "packet type must not be null");
        Objects.requireNonNull(condition, "condition must not be null");
        Objects.requireNonNull(action, "action must not be null");

        // get the set of waiting listeners for this packet type, add new waiting packet to the list
        WaitingPacket waitingPacket = new WaitingPacket<>(condition, action);
        Set<WaitingPacket> set = waitingPackets.computeIfAbsent(packet, c -> new HashSet<>());
        set.add(waitingPacket);

        if (timeout > 0 && unit != null) {
            waitingScheduler.schedule(() -> {
                // set#remove is ran regardless of whether or not timeoutAction is null due to it being evaluated first
                if (set.remove(waitingPacket) && timeoutAction != null) {
                    // run timeout action if one was specified
                    timeoutAction.run();
                }
            }, timeout, unit);
        }
    }

}
