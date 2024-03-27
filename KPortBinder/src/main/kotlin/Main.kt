package org.portbinder

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

fun main() {
    thread {
        Thread.sleep(1000)
        PortBinderAgent(localPort = 9876, serverOpenPort = 8889).let {
            it.start()
        }
    }
    thread {
        EchoServer().run()
    }
    PortBinderServer().run()
}

class EchoServer(private val port: Int = 9876) {
    fun run() {
        val server = ServerSocket(port)
        println("Echo Server is running on port ${server.localPort}")

        while (true) {
            val client = server.accept()
            thread {
                handleClient(client)
            }
        }
    }

    private fun handleClient(socket: Socket) {
        val output = PrintWriter(socket.getOutputStream(),true)
        val input = BufferedReader(InputStreamReader(socket.getInputStream()))

        while (true) {
            val line = input.readLine()
            output.println(line)
        }
    }
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
                server.open()
            }
        }
    }

    private fun handleProxy(clientId: ClientId, socket: Socket) {
        proxyServer?.addProxyClient(clientId, socket)
    }
}

class ProxyServer(
    private val port: Int,
    private val agentSocket: Socket
) {

    private val map = ConcurrentHashMap<String, Socket>()

    fun open() {
        val server = ServerSocket(port)
        println("Proxy Server is running on port ${server.localPort}")

        while (true) {
            val client = server.accept()
            println("Client connected: ${client.inetAddress.hostAddress}")

            thread { handleClient(client) }
        }
    }

    private fun handleClient(socket: Socket) {
        map[ClientId(socket).value] = socket
        val output = PrintWriter(agentSocket.getOutputStream(),true)
        output.println("proxy;${ClientId(socket).value}")
    }

    fun addProxyClient(clientId: ClientId, agentClientSocket: Socket) {
        val socket = map[clientId.value]
        if (socket != null) {
            SocketRoom(socket, agentClientSocket).run()
        }
    }
}

data class ClientId(val value: String) {
    constructor(socket: Socket) : this(socket.inetAddress.hostAddress + socket.port)
}

class SocketRoom(private val sa: Socket, private val sb: Socket) {
    fun run() {
        thread { connect(sa, sb) }
        thread { connect(sb, sa) }
    }

    private fun connect(socket: Socket, other: Socket) {
        val input = socket.getInputStream()
        val otherOutput = other.getOutputStream()

        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            println("data in ${bytesRead}bytes")
            otherOutput.write(buffer, 0, bytesRead)
        }
    }
}

class PortBinderAgent(
    private val serverUrl: String = "127.0.0.1",
    private val commandPort: Int = 9999,
    private val localPort: Int,
    private val serverOpenPort: Int
) {
    private var socket: Socket? = null

    fun start() {
        socket = Socket(serverUrl, commandPort)

        thread {
            socket?.let {
                val output = PrintWriter(it.getOutputStream(),true)
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

    fun createProxy(clientId: ClientId) {
        val localSocket = Socket("127.0.0.1", localPort)
        val serverSocket = Socket(serverUrl, commandPort)

        val serverOutput = PrintWriter(serverSocket.getOutputStream(), true)
        serverOutput.println("proxy;${clientId.value}")

        SocketRoom(localSocket, serverSocket).run()
    }
}