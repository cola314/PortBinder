package org.portbinder

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.portbinder.agent.PortBinderAgent
import org.portbinder.common.TestPort
import org.portbinder.common.withDelay
import org.portbinder.server.Client
import org.portbinder.server.EchoServer
import java.net.Socket
import kotlin.concurrent.thread

class PortBinderServerTest {
    @Test
    fun integrationTest() {
        val portBinderServerPort = TestPort.get()
        val portBinderServer = PortBinderServer(portBinderServerPort)
        thread { portBinderServer.run() }.withDelay()

        val localEchoServerPort = TestPort.get()
        val localEchoServer = EchoServer(port = localEchoServerPort)
        thread { localEchoServer.run() }.withDelay()

        val localhost = "127.0.0.1"
        val serverOpenPort = TestPort.get()
        val portBinderAgent = PortBinderAgent(
            serverUrl = localhost,
            serverPort = portBinderServerPort,
            localPort = localEchoServerPort,
            serverOpenPort = serverOpenPort,
        )
        thread { portBinderAgent.run() }.withDelay()

        val client = Client(Socket(localhost, serverOpenPort))
        val message = "hello world"
        client.println(message)

        val result = client.readLine()
        assertEquals(message, result)
    }
}