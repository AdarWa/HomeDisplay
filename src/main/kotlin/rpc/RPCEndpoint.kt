package net.adarw.rpc

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class RPCEndpoint(
    val inputType: KClass<*>,
    val outputType: KClass<*>,
    val topic: String = "",
    val isProtobuf: Boolean = true
)
