package org.portbinder

import org.portbinder.agent.PortBinderAgent
import org.portbinder.server.EchoServer
import org.portbinder.server.PortBinderServer

fun main(args: Array<String>) {
    val command = args.getOrNull(0)
    when (command) {
        "-server" -> runAsServer(args)
        "-agent" -> runAsAgent(args)
        "-echo" -> runAsEchoServer(args)
        else -> printHelpMessage()
    }
}

fun printHelpMessage() {
    val helpMessage = """argument: -server | -agent | -echo
        |-server <server-port>: run as port-binder server
        |-agent <server-url> <server-port> <local-port> <server-open-port>: run as port-binder agent
        |-echo: run as echo server for test
    """.trimMargin()

    print(helpMessage)
}

fun runAsServer(args: Array<String>) {
    val serverPort = args.getOrNull(1)?.toIntOrNull() ?: throw IllegalArgumentException("<server-port> required")
    PortBinderServer(port = serverPort).run()
}

fun runAsAgent(args: Array<String>) {
    val serverUrl = args.getOrNull(1) ?: throw IllegalArgumentException("<local-port> required")
    val serverPort = args.getOrNull(2)?.toIntOrNull() ?: throw IllegalArgumentException("<local-port> required")
    val localPort = args.getOrNull(3)?.toIntOrNull() ?: throw IllegalArgumentException("<local-port> required")
    val serverOpenPort = args.getOrNull(4)?.toIntOrNull() ?: throw IllegalArgumentException("<server-open-port> required")
    PortBinderAgent(
        serverUrl = serverUrl,
        serverPort = serverPort,
        localPort = localPort,
        serverOpenPort = serverOpenPort,
    ).run()
}

fun runAsEchoServer(args: Array<String>) {
    val serverPort = args.getOrNull(1)?.toIntOrNull() ?: throw IllegalArgumentException("<server-port> required")
    EchoServer(port = serverPort).run()
}
