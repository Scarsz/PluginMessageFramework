package github.scarsz.pluginmessageframework.forge;

import github.scarsz.pluginmessageframework.gateway.ServerGateway;
import github.scarsz.pluginmessageframework.internal.ServerGatewaySupport;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.io.IOException;
import java.util.List;

/**
 * The default Forge implementation of a {@link ServerGateway}.
 */
public class ImplForgeServerGateway extends ServerGatewaySupport<EntityPlayerMP> {

    FMLEventChannel channel;

    protected ImplForgeServerGateway(String channelName) {
        super(channelName);
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channelName);
        channel.register(this);
    }

    @Override
    public void sendPayload(EntityPlayerMP connection, byte[] bytes) {
        this.channel.sendTo(new FMLProxyPacket(new PacketBuffer(Unpooled.wrappedBuffer(bytes)), getChannel()), connection);
    }

    @Override
    @SuppressWarnings("unchecked")
    public EntityPlayerMP getConnection() {
        List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
        return players.size() > 0 ? players.iterator().next() : null;
    }

    @SubscribeEvent
    public void onPacketEvent(FMLNetworkEvent.ServerCustomPacketEvent event) {
        EntityPlayerMP client = ((NetHandlerPlayServer) event.getHandler()).player;
        if (event.getPacket().channel().equals(getChannel())) {
            try {
                incomingPayload(client, event.getPacket().payload().array());
            } catch (IOException e) {
                logger.error("Error handling incoming payload.", e);
            }
        }
    }

}
