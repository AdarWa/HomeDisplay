package net.adarw.rpc.definitions.messages

data class RegisterMessage(
    override val statusCode: Int,
    override val senderId: Int,
    val serialId: String
) : RPCMessage()

data class RegisterResponse(
    override val statusCode: Int,
    override val senderId: Int
) : RPCResponse()