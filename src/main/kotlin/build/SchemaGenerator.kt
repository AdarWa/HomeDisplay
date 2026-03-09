package net.adarw.build

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.schema.ProtoBufSchemaGenerator
import net.adarw.rpc.definitions.messages.RegisterMessage
import java.io.File

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProtoGenerate

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val descriptors = listOf(
        RegisterMessage.serializer().descriptor
    )

    val schemaText = ProtoBufSchemaGenerator.generateSchemaText(
        descriptors = descriptors,
    )

    val outputFile = File("src/main/proto/kotlinSchemas.proto")
    outputFile.parentFile.mkdirs()
    outputFile.writeText(schemaText)

    println("Protobuf schema successfully generated at ${outputFile.absolutePath}")
}