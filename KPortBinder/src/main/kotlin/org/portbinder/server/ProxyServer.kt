package org.portbinder.server

import org.portbinder.tcp.ClientId
import org.portbinder.tcp.SocketRoom
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

class ProxyServer(
    port: Int,
    private val agentSocket: Socket
): TcpServer(port) {

    override val name = "ProxyServer"

    private val map = ConcurrentHashMap<String, Client>()

    override fun handleClient(client: Client) {
        map[ClientId(client).value] = client
        Client(agentSocket).println("proxy;${ClientId(client).value}")
    }

    fun addProxyClient(clientId: ClientId, agentClientSocket: Socket) {
        val client = map[clientId.value]
        if (client != null) {
            SocketRoom(client.socket, agentClientSocket).run()
        }
    }
}