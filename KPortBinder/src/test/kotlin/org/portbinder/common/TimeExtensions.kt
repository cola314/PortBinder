package org.portbinder.common

fun Unit.withDelay(i: Long = 100) {
    Thread.sleep(i)
}

fun <T> T.withDelay(i: Long = 100): T {
    Thread.sleep(i)
    return this
}