package net.adarw.rpc.states

import kotlinx.serialization.Serializable
import net.adarw.build.ProtoGenerate
import net.adarw.rpc.RPCEndpoint
import net.adarw.rpc.definitions.messages.Ping
import net.adarw.rpc.definitions.messages.Pong
import net.adarw.rpc.definitions.messages.RPCMessage
import net.adarw.rpc.definitions.messages.RPCResponse
import net.adarw.rpc.definitions.messages.RegisterMessage

@RPCEndpoint(RegisterMessage::class, Unit::class)
fun registerDevice(msg: RegisterMessage) {}

@RPCEndpoint(Ping::class, Pong::class)
fun ping(msg: Ping): Pong {
    return Pong(msg.str)
}
