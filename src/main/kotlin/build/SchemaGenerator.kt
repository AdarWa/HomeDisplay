package net.adarw.build

import java.io.File
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.protobuf.schema.ProtoBufSchemaGenerator
import kotlinx.serialization.serializer
import org.reflections.Reflections

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProtoGenerate

@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
fun main() {
    println("Scanning for @ProtoGenerate annotations...")
    val reflections = Reflections("net.adarw")

    val annotatedClasses =
        reflections.getTypesAnnotatedWith(ProtoGenerate::class.java)

    if (annotatedClasses.isEmpty()) {
        println("Warning: No classes found with @ProtoGenerate annotation.")
        return
    }

    val descriptors =
        annotatedClasses.map { clazz ->
            try {
                serializer(clazz).descriptor
            } catch (e: Exception) {
                throw RuntimeException(
                    "Failed to get serializer for ${clazz.name}. Is it marked with @Serializable?",
                    e,
                )
            }
        }

    val schemaText =
        ProtoBufSchemaGenerator.generateSchemaText(descriptors = descriptors)

    val outputFile = File("src/main/proto/kotlinSchemas.proto")
    outputFile.parentFile.mkdirs()
    outputFile.writeText(schemaText)

    println(
        "Protobuf schema successfully generated at ${outputFile.absolutePath}"
    )
    println(
        "Included ${annotatedClasses.size} messages: ${annotatedClasses.joinToString { it.simpleName }}"
    )
}
