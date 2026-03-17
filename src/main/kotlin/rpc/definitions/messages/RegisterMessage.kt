package net.adarw.rpc.definitions.messages

import kotlinx.serialization.Serializable
import net.adarw.build.ProtoGenerate

@ProtoGenerate
@Serializable
data class RegisterMessage(
    override val statusCode: Int,
    override val senderId: Int,
    val serialId: String,
) : RPCMessage()

@ProtoGenerate
@Serializable
data class RegisterResponse(
    override val statusCode: Int,
    override val senderId: Int,
    val givenId: Int,
) : RPCResponse()
