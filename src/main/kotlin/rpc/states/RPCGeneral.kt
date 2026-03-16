package net.adarw.rpc.states

import mu.KotlinLogging
import net.adarw.rpc.HOST
import net.adarw.rpc.RPCEndpoint
import net.adarw.rpc.definitions.StatusCode
import net.adarw.rpc.definitions.messages.RegisterMessage
import net.adarw.rpc.definitions.messages.RegisterResponse
import net.adarw.store.rpc.RPCStoreManager

private val logger = KotlinLogging.logger {}

@RPCEndpoint(
    RegisterMessage::class,
    RegisterResponse::class,
    isProtobuf = false,
)
fun registerDevice(msg: RegisterMessage): RegisterResponse {
    if (msg.serialId.isEmpty()) {
        logger.error {
            "serialId cannot be empty while registering new device!"
        }
        return RegisterResponse(StatusCode.BAD_REQUEST.code, HOST, -1)
    }
    if (RPCStoreManager.serialIdExists(msg.serialId)) {
        logger.error {
            "serialId ${msg.serialId} already exists in the registry!"
        }
        return RegisterResponse(StatusCode.CONFLICT.code, HOST, -1)
    }
    val rpcId = RPCStoreManager.registerRPC(msg.serialId)
    logger.info {
        "Registering Device with serial id ${msg.serialId}, given id $rpcId"
    }
    return RegisterResponse(StatusCode.CREATED.code, HOST, rpcId)
}
