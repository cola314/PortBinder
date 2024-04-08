package org.portbinder.agent

import org.portbinder.command.Command
import org.portbinder.server.Client
import org.portbinder.tcp.ClientId
import org.portbinder.tcp.SocketRoom
import java.net.Socket

class PortBinderAgent(
    private val serverUrl: String = "127.0.0.1",
    private val serverPort: Int = 9999,
    private val localPort: Int,
    private val serverOpenPort: Int
) {
    private var client: Client? = null

    fun run() {
        client = Client(Socket(serverUrl, serverPort))
        client?.let {
            it.println("agent;${serverOpenPort}")

            while (true) {
                val command = Command.parse(it.readLine())

                when (command.type) {
                    "proxy" -> createProxy(ClientId(command.arg))
                }
            }
        }
    }

    private fun createProxy(clientId: ClientId) {
        val localClient = Client(Socket("127.0.0.1", localPort))
        val serverClient = Client(Socket(serverUrl, serverPort))

        serverClient.println("proxy;${clientId.value}")

        SocketRoom(localClient, serverClient).run()
    }
}