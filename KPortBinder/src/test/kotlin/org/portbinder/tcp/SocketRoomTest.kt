package org.portbinder.tcp

import org.junit.jupiter.api.Test
import org.portbinder.common.TestPort
import org.portbinder.common.withDelay
import org.portbinder.server.mock.MockServer
import org.portbinder.tcp.SocketRoom
import kotlin.concurrent.thread
import kotlin.test.assertEquals

class SocketRoomTest {

    @Test
    fun connectTest() {
        val port1 = TestPort.get()
        val port2 = TestPort.get()
        val (server1, client1) = MockServer.generateMockServerAndClient(port1)
        val (server2, client2) = MockServer.generateMockServerAndClient(port2)

        thread {
            SocketRoom(client1.socket, client2.socket).run()
        }.withDelay()

        val message = "hello world"
        server1.println(message).withDelay()
        val result = server2.readLine()

        assertEquals(message, result)
    }
}