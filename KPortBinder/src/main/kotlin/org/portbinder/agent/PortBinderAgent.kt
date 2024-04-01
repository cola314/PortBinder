package org.portbinder.agent

import org.portbinder.server.Client
import org.portbinder.tcp.ClientId
import org.portbinder.tcp.SocketRoom
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class PortBinderAgent(
    private val serverUrl: String = "127.0.0.1",
    private val serverPort: Int = 9999,
    private val localPort: Int,
    private val serverOpenPort: Int
) {
    private var socket: Socket? = null

    fun run() {
        socket = Socket(serverUrl, serverPort)

        thread {
            socket?.let {
                val output = PrintWriter(it.getOutputStream(), true)
                output.println("agent;${serverOpenPort}")

                val input = BufferedReader(InputStreamReader(it.getInputStream()))
                while (true) {
                    val line = input.readLine()
                    val tokens = line.split(';')
                    val command = tokens[0]
                    println("agent command in: ${line}")
                    when (command) {
                        "proxy" -> createProxy(ClientId(tokens[1]))
                    }
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