package net.adarw.rpc.definitions.messages

import kotlinx.serialization.Serializable

@Serializable
data class RegisterMessage(
    override val statusCode: Int,
    override val senderId: Int,
    val serialId: String
) : RPCMessage()

data class RegisterResponse(
    override val statusCode: Int,
    override val senderId: Int
) : RPCResponse()