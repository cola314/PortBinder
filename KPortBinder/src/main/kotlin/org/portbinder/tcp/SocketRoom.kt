package org.portbinder.tcp

import org.portbinder.server.Client
import java.net.Socket
import kotlin.concurrent.thread

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
        runCatching {
            while (input.read(buffer).also { bytesRead = it } != -1) {
                println("data in ${bytesRead}bytes")
                otherOutput.write(buffer, 0, bytesRead)
            }
        }.onFailure { other.close() }
    }
}