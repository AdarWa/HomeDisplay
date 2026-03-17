package net.adarw

import mu.KotlinLogging
import net.adarw.rpc.RPCHandler

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Starting HomeDisplay..." }
    RPCHandler
}
