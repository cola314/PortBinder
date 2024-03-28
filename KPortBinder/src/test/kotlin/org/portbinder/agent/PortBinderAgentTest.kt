package org.portbinder.agent

import org.junit.jupiter.api.Test
import org.portbinder.common.TestPort
import org.portbinder.common.withDelay
import org.portbinder.server.mock.MockServer
import kotlin.concurrent.thread
import kotlin.test.assertEquals

class PortBinderAgentTest {

    @Test
    fun agentConnectedMessageTest() {
        val localhost = "127.0.0.1"
        val serverPort = TestPort.get()
        val localPort = TestPort.get()
        val serverOpenPort = TestPort.get()
        val server = MockServer.generateMockServer(serverPort)

        val agent = PortBinderAgent(
            serverUrl = localhost,
            serverPort = serverPort,
            localPort = localPort,
            serverOpenPort = serverOpenPort)

        thread { agent.run() }.withDelay()

        val result = server.readLine()
        val message = "agent;${serverOpenPort}"
        assertEquals(message, result)
    }

    @Test
    fun agentProxyCommandTest() {
        val localhost = "127.0.0.1"
        val serverPort = TestPort.get()
        val localPort = TestPort.get()
        val serverOpenPort = TestPort.get()
        val server = MockServer.generateMockServer(serverPort)
        MockServer.generateMockServer(localPort)

        val agent = PortBinderAgent(
            serverUrl = localhost,
            serverPort = serverPort,
            localPort = localPort,
            serverOpenPort = serverOpenPort)

        thread { agent.run() }.withDelay()

        val message = "proxy;clientId"
        server.println(message).withDelay()

        val result = server.readLine()
        assertEquals(message, result)
    }

    @Test
    fun agentProxyTest_localServerMessageTransferToServer() {
        val localhost = "127.0.0.1"
        val serverPort = TestPort.get()
        val localPort = TestPort.get()
        val serverOpenPort = TestPort.get()
        val server = MockServer.generateMockServer(serverPort)
        val localServer = MockServer.generateMockServer(localPort)

        val agent = PortBinderAgent(
            serverUrl = localhost,
            serverPort = serverPort,
            localPort = localPort,
            serverOpenPort = serverOpenPort)

        thread { agent.run() }.withDelay()

        server.println("proxy;clientId").withDelay()
        server.readLine() // read proxy command

        val message = "hello world"
        localServer.println(message).withDelay()

        val result = server.readLine()
        assertEquals(message, result)
    }

    @Test
    fun agentProxyTest_serverMessageTransferToLocalServer() {
        val localhost = "127.0.0.1"
        val serverPort = TestPort.get()
        val localPort = TestPort.get()
        val serverOpenPort = TestPort.get()
        val server = MockServer.generateMockServer(serverPort)
        val localServer = MockServer.generateMockServer(localPort)

        val agent = PortBinderAgent(
            serverUrl = localhost,
            serverPort = serverPort,
            localPort = localPort,
            serverOpenPort = serverOpenPort)

        thread { agent.run() }.withDelay()

        server.println("proxy;clientId").withDelay()
        server.readLine() // read proxy command

        val message = "hello world"
        server.println(message).withDelay()

        val result = localServer.readLine()
        assertEquals(message, result)
    }
}