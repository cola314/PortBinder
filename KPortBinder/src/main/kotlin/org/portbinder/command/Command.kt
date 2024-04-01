package org.portbinder.command

data class Command(val type: String, val arg: String) {
    companion object {
        fun parse(input: String): Command {
            val token = input.split(';')
            if (token.size != 2) {
                println("invalid token")
                throw IllegalArgumentException()
            }

            return Command(token[0], token[1])
        }
    }
}