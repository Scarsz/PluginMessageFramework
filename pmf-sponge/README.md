Sponge Implementation
============

Usage
-----

```java
public ServerGateway<Player> gateway;

@Listener
public void onServerStart(GameStartedServerEvent event) {
    gateway = SpongeGatewayProvider.getGateway("MyChannelName", this);
    gateway.registerListener(this);
}

@PacketHandler
public void onMyPacket(Player player, MyPacket myPacket) {
    player.sendMessage("Received message: " + myPacket.getMessage());
}
```