package org.portbinder

import org.portbinder.agent.PortBinderAgent
import org.portbinder.server.EchoServer
import org.portbinder.server.ProxyServer
import org.portbinder.tcp.ClientId
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

fun main() {
    thread {
        Thread.sleep(1000)
        PortBinderAgent(localPort = 9876, serverOpenPort = 8889).let {
            it.run()
        }
    }
    thread {
        EchoServer().run()
    }
    PortBinderServer().run()
}

class PortBinderServer(private val commandPort: Int = 9999) {

    private var proxyServer: ProxyServer? = null
    private var agentSocket: Socket? = null

    fun run() {
        val server = ServerSocket(commandPort)
        println("Server is running on port ${server.localPort}")

        while (true) {
            val client = server.accept()
            println("Client connected: ${client.inetAddress.hostAddress}")

            thread { handleClient(client) }
        }
    }

    private fun handleClient(socket: Socket) {
        val output = PrintWriter(socket.getOutputStream(),true)
        val input = BufferedReader(InputStreamReader(socket.getInputStream()))

        val line = input.readLine()
        val tokens = line.split(';')
        val command = tokens[0]
        println("command in ${command}")

        when (command) {
            "agent" -> handleOpen(tokens[1], socket)
            "proxy" -> handleProxy(ClientId(tokens[1]), socket)
        }
    }

    private fun handleOpen(arg: String, socket: Socket) {
        agentSocket = socket
        val port = arg.toIntOrNull() ?: throw IllegalArgumentException()
        thread {
            agentSocket?.let {
                val server = ProxyServer(port, it)
                proxyServer = server
                server.run()
            }
        }
    }

    private fun handleProxy(clientId: ClientId, socket: Socket) {
        proxyServer?.addProxyClient(clientId, socket)
    }
}
