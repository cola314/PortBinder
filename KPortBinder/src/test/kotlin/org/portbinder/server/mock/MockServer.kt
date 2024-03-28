package org.portbinder.server.mock

import org.portbinder.server.Client
import org.portbinder.server.TcpServer
import org.portbinder.common.withDelay
import java.net.Socket
import kotlin.concurrent.thread

data class MockData(val mockServer: MockServer, val client: Client)
class MockServer(port: Int) : TcpServer(port) {
    override val name = "Mock Server"
    private lateinit var client: Client

    companion object {
        fun generateMockServer(port: Int): MockServer {
            val server = MockServer(port)
            thread { server.run() }.withDelay()
            return server
        }

        fun generateMockServerAndClient(port: Int): MockData {
            val server = MockServer(port)
            thread {
                server.run()
            }.withDelay()
            val client = Socket("127.0.0.1", port).withDelay()
            return MockData(server, Client(client))
        }
    }

    override fun handleClient(client: Client) {
        this.client = client
    }

    fun println(data: String) {
        client.println(data)
    }

    fun readLine(): String {
        return client.readLine()
    }
}