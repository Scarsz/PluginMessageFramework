package github.scarsz.pluginmessageframework.forge;

import github.scarsz.pluginmessageframework.gateway.ClientGateway;
import github.scarsz.pluginmessageframework.internal.ClientGatewaySupport;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.io.IOException;

/**
 * The default Forge implementation of a {@link ClientGateway}.
 */
public class ImplForgeClientGateway extends ClientGatewaySupport<FMLEventChannel> {

    protected ImplForgeClientGateway(String channelName) {
        super(channelName, NetworkRegistry.INSTANCE.newEventDrivenChannel(channelName));
        connection.register(this);
    }

    @Override
    public void sendPayload(FMLEventChannel connection, byte[] bytes) {
        this.connection.sendToServer(new FMLProxyPacket(new PacketBuffer(Unpooled.wrappedBuffer(bytes)), getChannel()));
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        if (event.getPacket().channel().equals(getChannel())) {
            try {
                incomingPayload(connection, event.getPacket().payload().array());
            } catch (IOException e) {
                logger.error("Error handling incoming payload.", e);
            }
        }
    }
}
