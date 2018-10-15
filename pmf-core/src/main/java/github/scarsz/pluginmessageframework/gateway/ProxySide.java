package github.scarsz.pluginmessageframework.gateway;

/**
 * An enum containing all possible sides a {@link ProxyGateway} can be run on.
 */
public enum ProxySide {

    /**
     * For Proxy &lt;-&gt; Client connections (e.g. BungeeCord to client-side Forge connections).
     */
    CLIENT,

    /**
     * For Proxy &lt;-&gt; Server connections (e.g. BungeeCord to Bukkit/Spigot connections).
     */
    SERVER

}
