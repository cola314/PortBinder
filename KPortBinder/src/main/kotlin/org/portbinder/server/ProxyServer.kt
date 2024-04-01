package org.portbinder.server

import org.portbinder.tcp.ClientId
import org.portbinder.tcp.SocketRoom
import java.util.concurrent.ConcurrentHashMap

class ProxyServer(
    port: Int,
    private val agentClient: Client
): TcpServer(port) {

    override val name = "ProxyServer"

    private val map = ConcurrentHashMap<String, Client>()

    override fun handleClient(client: Client) {
        map[ClientId(client).value] = client
        agentClient.println("proxy;${ClientId(client).value}")
    }

    fun addProxyClient(clientId: ClientId, agentClient: Client) {
        val client = map[clientId.value]
        if (client != null) {
            SocketRoom(client, agentClient).run()
        }
    }
}