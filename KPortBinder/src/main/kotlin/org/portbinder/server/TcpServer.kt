package org.portbinder.server

import java.net.ServerSocket
import kotlin.concurrent.thread

abstract class TcpServer(
    private val port: Int,
) {
    abstract val name: String

    private var serverSocket: ServerSocket? = null

    fun run() {
        val server = ServerSocket(port)
        serverSocket = server
        println("${name} Server is running on port ${server.localPort}")

        while (true) {
            val client = server.accept()
            println("${name} Server Client connected ${client.inetAddress}")

            thread {
                handleClient(Client(client))
            }
        }
    }

    protected abstract fun handleClient(client: Client)

    fun close() {
        serverSocket?.close()
    }
}