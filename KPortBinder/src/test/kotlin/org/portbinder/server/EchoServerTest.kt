package org.portbinder.server

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.portbinder.common.TestPort
import org.portbinder.common.withDelay
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class EchoServerTest {

    @Test
    fun echoTest() {
        val localhost = "127.0.0.1"
        val port = TestPort.get()
        val message = "hello world"
        thread {
            val server = EchoServer(port)
            server.run()
        }.withDelay()
        val client = Socket(localhost, port)
        PrintWriter(client.getOutputStream(), true).println(message).withDelay()
        val result = BufferedReader(InputStreamReader(client.getInputStream())).readLine()

        assertEquals(message, result)
    }
}