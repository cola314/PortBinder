package org.portbinder

import org.portbinder.agent.PortBinderAgent
import org.portbinder.server.EchoServer
import org.portbinder.server.PortBinderServer
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

