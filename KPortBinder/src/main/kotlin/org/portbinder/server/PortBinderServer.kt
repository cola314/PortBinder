package org.portbinder.server

import org.portbinder.tcp.ClientId
import kotlin.concurrent.thread

class PortBinderServer(
    port: Int = 9999,
): TcpServer(port) {

    override val name = "PortBinderServer"

    private var proxyServer: ProxyServer? = null
    private var agentClient: Client? = null

    override fun handleClient(client: Client) {
        val line = client.readLine()
        val tokens = line.split(';')
        val command = tokens[0]
        println("command in ${command}")

        when (command) {
            "agent" -> handleOpen(tokens[1], client)
            "proxy" -> handleProxy(ClientId(tokens[1]), client)
        }
    }

    private fun handleOpen(arg: String, client: Client) {
        agentClient = client
        val port = arg.toIntOrNull() ?: throw IllegalArgumentException()
        thread {
            agentClient?.let {
                val server = ProxyServer(port, it)
                proxyServer = server
                server.run()
            }
        }
    }

    private fun handleProxy(clientId: ClientId, client: Client) {
        proxyServer?.addProxyClient(clientId, client)
    }
}