package net.adarw.rpc.states

import mu.KotlinLogging
import net.adarw.rpc.HOST
import net.adarw.rpc.RPCEndpoint
import net.adarw.rpc.definitions.StatusCode
import net.adarw.rpc.definitions.messages.RegisterMessage
import net.adarw.rpc.definitions.messages.RegisterResponse

private val logger = KotlinLogging.logger {  }

@RPCEndpoint(RegisterMessage::class, RegisterResponse::class, isProtobuf = false)
fun registerDevice(msg: RegisterMessage): RegisterResponse {
    logger.info { "Registering Device with serial id ${msg.serialId}" }
    return RegisterResponse(StatusCode.CREATED.code, HOST, )
}
