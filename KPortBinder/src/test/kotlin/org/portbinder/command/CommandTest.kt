package org.portbinder.command

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CommandTest {
    @Test
    fun parseTest() {
        val input = "agent;hello"
        
        val command = Command.parse(input)

        assertEquals("agent", command.type)
        assertEquals("hello", command.arg)
    }
}