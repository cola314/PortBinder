package org.portbinder.tcp

import org.portbinder.server.Client
import java.net.Socket

data class ClientId(val value: String) {
    constructor(socket: Socket) : this(socket.inetAddress.hostAddress + socket.port)
    constructor(client: Client) : this(client.socket)
}