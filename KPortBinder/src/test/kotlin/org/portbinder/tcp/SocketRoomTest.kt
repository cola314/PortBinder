package org.portbinder.tcp

import org.junit.jupiter.api.Test
import org.portbinder.common.TestPort
import org.portbinder.common.withDelay
import org.portbinder.server.mock.MockServer
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SocketRoomTest {

    @Test
    fun connectTest() {
        val generateServerAndClient = { MockServer.generateMockServerAndClient(TestPort.get()) }
        val (server1, client1) = generateServerAndClient()
        val (server2, client2) = generateServerAndClient()

        thread { SocketRoom(client1, client2).run() }.withDelay()

        val message = "hello world"
        server1.println(message).withDelay()
        val result = server2.readLine()

        assertEquals(message, result)
    }

    @Test
    fun disconnectTest() {
        val generateServerAndClient = { MockServer.generateMockServerAndClient(TestPort.get()) }
        val (_, client1) = generateServerAndClient()
        val (_, client2) = generateServerAndClient()

        thread { SocketRoom(client1, client2).run() }.withDelay()

        client1.socket.close().withDelay()

        assertTrue(client2.socket.isClosed)
    }
}