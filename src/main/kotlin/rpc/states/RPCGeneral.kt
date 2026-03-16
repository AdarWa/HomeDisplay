package net.adarw.rpc.states

import net.adarw.rpc.RPCEndpoint
import net.adarw.rpc.definitions.messages.RegisterMessage

@RPCEndpoint(RegisterMessage::class, Unit::class)
fun registerDevice(msg: RegisterMessage) {}
