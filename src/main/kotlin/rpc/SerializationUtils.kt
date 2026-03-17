@file:OptIn(
    ExperimentalUnsignedTypes::class,
    ExperimentalSerializationApi::class,
)

package net.adarw.rpc

import kotlin.reflect.KClass
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer

fun <T : Any> deserializeMessage(
    payload: UByteArray,
    inputType: KClass<T>,
    isProtobuf: Boolean = true,
): T {
    val byteArray = payload.toByteArray()

    val serializer = serializer(inputType.java)

    @Suppress("UNCHECKED_CAST")
    return if (isProtobuf) {
        ProtoBuf.decodeFromByteArray(serializer, byteArray)
    } else {
        val jsonString = byteArray.decodeToString()
        Json.decodeFromString(serializer, jsonString)
    }
        as T
}

fun serializeMessage(
    msg: Any,
    outputType: KClass<*>,
    isProtobuf: Boolean = true,
): UByteArray {
    val serializer = serializer(outputType.java)

    val byteArray =
        if (isProtobuf) {
            ProtoBuf.encodeToByteArray(serializer, msg)
        } else {
            Json.encodeToString(serializer, msg).encodeToByteArray()
        }

    return byteArray.toUByteArray()
}

fun String.toResponsePath(): String {
    val match =
        pathRegex.find(this)
            ?: error(
                "Invalid request path: '$this'. Expected format: HomeDisplay/rpc/request/{id}/{path}"
            )

    val (id, tail) = match.destructured
    return "HomeDisplay/rpc/response/$id/$tail"
}
