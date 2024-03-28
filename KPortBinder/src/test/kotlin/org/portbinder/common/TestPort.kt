package org.portbinder.common

import java.util.concurrent.atomic.AtomicInteger

object TestPort {

    private val startPort = AtomicInteger(10000)
    fun get(): Int {
        return startPort.getAndIncrement()
    }
}