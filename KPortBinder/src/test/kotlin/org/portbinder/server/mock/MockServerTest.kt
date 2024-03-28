package org.portbinder.server.mock

import org.junit.jupiter.api.Test
import org.portbinder.common.TestPort
import org.portbinder.common.withDelay
import kotlin.test.assertEquals

class MockServerTest {

    @Test
    fun mockSererTest() {
        val port = TestPort.get()
        val data = "hello world"
        val (server, client) = MockServer.generateMockServerAndClient(port)
        server.println(data).withDelay()

        val result = client.readLine()
        assertEquals(data, result)
    }
}
