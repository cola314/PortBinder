package org.portbinder.server

import org.portbinder.command.Command
import org.portbinder.tcp.ClientId
import kotlin.concurrent.thread

class PortBinderServer(
    port: Int = 9999,
): TcpServer(port) {

    override val name = "PortBinderServer"

    private var proxyServer: ProxyServer? = null
    private var agentClient: Client? = null

    override fun handleClient(client: Client) {
        val command = Command.parse(client.readLine())

        when (command.type) {
            "agent" -> handleOpen(command.arg, client)
            "proxy" -> handleProxy(ClientId(command.arg), client)
        }
    }

    private fun handleOpen(arg: String, client: Client) {
        agentClient?.socket?.close()
        proxyServer?.close()

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