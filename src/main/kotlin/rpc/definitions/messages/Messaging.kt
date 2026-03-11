package net.adarw.rpc.definitions.messages

sealed class RPCMessage {
    abstract val statusCode: Int
    abstract val senderId: Int // Individual sender id, -1 means host
}

sealed class RPCResponse : RPCMessage()

data class RPCError(
    override val statusCode: Int,
    override val senderId: Int,
    val cause: String,
) : RPCMessage()
