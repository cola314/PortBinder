package org.portbinder.server

import org.portbinder.tcp.ClientId
import org.portbinder.tcp.SocketRoom
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class ProxyServer(
    private val port: Int,
    private val agentSocket: Socket
) {

    private val map = ConcurrentHashMap<String, Socket>()

    fun run() {
        val server = ServerSocket(port)
        println("Proxy Server is running on port ${server.localPort}")

        while (true) {
            val client = server.accept()
            println("Client connected: ${client.inetAddress.hostAddress}")

            thread { handleClient(client) }
        }
    }

    private fun handleClient(socket: Socket) {
        map[ClientId(socket).value] = socket
        val output = PrintWriter(agentSocket.getOutputStream(), true)
        output.println("proxy;${ClientId(socket).value}")
    }

    fun addProxyClient(clientId: ClientId, agentClientSocket: Socket) {
        val socket = map[clientId.value]
        if (socket != null) {
            SocketRoom(socket, agentClientSocket).run()
        }
    }
}