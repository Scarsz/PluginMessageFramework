package github.scarsz.pluginmessageframework.forge;

import github.scarsz.pluginmessageframework.gateway.ClientGateway;
import github.scarsz.pluginmessageframework.gateway.ServerGateway;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.FMLEventChannel;

/**
 * Provides a {@link ClientGateway} capable of running on the Forge implementation.
 */
public class ForgeGatewayProvider {

    /**
     * Gets a new {@link ClientGateway} capable of running on the Forge implementation.
     *
     * @param channel the channel to operate on
     * @return the gateway
     */
    public static ClientGateway<FMLEventChannel> getClientGateway(String channel) {
        return new ImplForgeClientGateway(channel);
    }

    /**
     * Gets a new {@link ServerGateway} capable of running on the Forge implementation.
     *
     * @param channel the channel to operate on
     * @return the gateway
     */
    public static ServerGateway<EntityPlayerMP> getServerGateway(String channel) {
        return new ImplForgeServerGateway(channel);
    }

    private ForgeGatewayProvider() {
    }

}
