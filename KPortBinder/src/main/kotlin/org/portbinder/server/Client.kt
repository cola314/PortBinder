package org.portbinder.server

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class Client(val socket: Socket) {
    private val output: PrintWriter = PrintWriter(socket.getOutputStream(), true)
    private val input: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))

    fun readLine(): String {
        return input.readLine()
    }

    fun println(data: String) {
        output.println(data)
    }
}