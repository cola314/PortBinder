package org.portbinder.tcp

import java.net.Socket

data class ClientId(val value: String) {
    constructor(socket: Socket) : this(socket.inetAddress.hostAddress + socket.port)
}