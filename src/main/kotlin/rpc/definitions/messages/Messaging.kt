package net.adarw.rpc.definitions.messages

import kotlinx.serialization.Serializable

sealed class RPCMessage {
    abstract val statusCode: Int
    abstract val senderId: Int // Individual sender id, -1 means host
}

sealed class RPCResponse : RPCMessage()

@Serializable
data class RPCError(
    override val statusCode: Int,
    override val senderId: Int,
    val cause: String,
) : RPCMessage()

@Serializable
data class RPCStatus(override val statusCode: Int, override val senderId: Int) :
    RPCMessage()

@Serializable data class Ping(val str: String)

@Serializable data class Pong(val echo: String)
