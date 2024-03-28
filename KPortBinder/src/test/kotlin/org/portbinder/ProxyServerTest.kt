package org.portbinder

import org.junit.jupiter.api.Test
import org.portbinder.common.TestPort
import org.portbinder.common.withDelay
import org.portbinder.server.Client
import org.portbinder.server.ProxyServer
import org.portbinder.server.mock.MockServer
import org.portbinder.tcp.ClientId
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProxyServerTest {

    @Test
    fun proxyServerTest() {
        val proxyServerPort = TestPort.get()
        val serverPort = TestPort.get()
        val agentProxyPort = TestPort.get()
        val (agent, server) = MockServer.generateMockServerAndClient(serverPort)
        val (agentProxy, agentClient) = MockServer.generateMockServerAndClient(agentProxyPort)
        val proxyServer = ProxyServer(proxyServerPort, server.socket)

        thread { proxyServer.run() }.withDelay()

        val localhost = "127.0.0.1"
        val proxyClient = Client(Socket(localhost, proxyServerPort)).withDelay()

        val result = agent.readLine()
        assertTrue(result.startsWith("proxy;127.0.0.1"))

        val clientId = ClientId(result.split(';')[1])
        proxyServer.addProxyClient(clientId, agentClient.socket)

        val message = "hello world"
        proxyClient.println(message)
        assertEquals(message, agentProxy.readLine())
    }
}