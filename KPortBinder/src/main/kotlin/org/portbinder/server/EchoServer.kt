package org.portbinder.server

class EchoServer(port: Int = 9876): TcpServer(port) {

    override val name = "EchoServer"

    override fun handleClient(client: Client) {
        while (true) {
            val line = client.readLine()
            client.println(line)
        }
    }
}