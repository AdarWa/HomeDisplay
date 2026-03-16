package net.adarw.rpc.processor

import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

@SupportedOptions(RpcEndpointProcessor.KAPT_KOTLIN_GENERATED)
@SupportedAnnotationTypes("net.adarw.rpc.RPCEndpoint")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
class RpcEndpointProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"
    }

    private var hasGenerated = false

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment,
    ): Boolean {
        if (hasGenerated) return false

        val annotationType =
            processingEnv.elementUtils.getTypeElement("net.adarw.rpc.RPCEndpoint") ?: return false

        val endpoints =
            roundEnv.getElementsAnnotatedWith(annotationType)
                .filter { it.kind == ElementKind.METHOD }
                .mapNotNull { element ->
                    val executable = element as ExecutableElement
                    if (executable.modifiers.contains(Modifier.PRIVATE)) {
                        processingEnv.messager.printMessage(
                            Diagnostic.Kind.WARNING,
                            "Skipping private RPCEndpoint: ${executable.simpleName}",
                            executable,
                        )
                        return@mapNotNull null
                    }
                    buildEndpoint(executable)
                }
                .sortedBy { it.topic }

        if (endpoints.isEmpty() && !roundEnv.processingOver()) return false

        val outputDir = processingEnv.options[KAPT_KOTLIN_GENERATED]
        if (outputDir == null) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Missing kapt.kotlin.generated option",
            )
            return false
        }

        val outputFile = File(outputDir, "net/adarw/rpc/RPCRegistry.kt")
        outputFile.parentFile.mkdirs()
        outputFile.writeText(renderRegistry(endpoints))
        hasGenerated = true
        return false
    }

    private fun buildEndpoint(element: ExecutableElement): Endpoint? {
        val enclosingType = element.enclosingElement as? TypeElement ?: return null
        val annotation = findAnnotation(element)
        val inputType = resolveAnnotationType(annotation, "inputType")
        val outputType = resolveAnnotationType(annotation, "outputType")
        val isProtobuf = resolveAnnotationSerializationType(annotation, "isProtobuf")

        // Extract package and name data
        val packageName = processingEnv.elementUtils.getPackageOf(element).qualifiedName.toString()
        val className = enclosingType.simpleName.toString()
        val functionName = element.simpleName.toString()
        val topic = resolveTopic(element).takeIf { it?.isNotEmpty() ?: false } ?: functionName

        // KAPT compiles top-level Kotlin functions as STATIC methods
        val isTopLevel = element.modifiers.contains(Modifier.STATIC)

        if (element.parameters.size != 1) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.WARNING,
                "RPCEndpoint should have exactly one parameter; found ${element.parameters.size}",
                element,
            )
        } else if (inputType != null) {
            val parameterType = element.parameters.first().asType()
            if (!processingEnv.typeUtils.isSameType(parameterType, inputType)) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "RPCEndpoint inputType does not match parameter type: expected $inputType, found $parameterType",
                    element,
                )
            }
            if (!isSerializable(inputType)) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "RPCEndpoint inputType $inputType is not marked @Serializable",
                    element,
                )
            }
        }

        if (outputType != null) {
            val returnType = element.returnType
            if (!processingEnv.typeUtils.isSameType(returnType, outputType)) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "RPCEndpoint outputType does not match return type: expected $outputType, found $returnType",
                    element,
                )
            }
            if (!isSerializable(outputType)) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "RPCEndpoint outputType $outputType is not marked @Serializable",
                    element,
                )
            }
        }

        return Endpoint(
            packageName = packageName,
            className = className,
            functionName = functionName,
            topic = topic,
            isTopLevel = isTopLevel,
            isProtobuf = isProtobuf != false,
            inputType = inputType?.toString(),
            outputType = outputType?.toString()
        )
    }

    private fun resolveTopic(element: ExecutableElement): String? {
        val annotation = findAnnotation(element) ?: return null
        val valuesMap = processingEnv.elementUtils.getElementValuesWithDefaults(annotation)

        val topicEntry =
            valuesMap.entries.firstOrNull { entry ->
                entry.key.simpleName.toString() == "topic"
            } ?: return null

        return topicEntry.value.value as? String
    }

    private fun findAnnotation(element: ExecutableElement): AnnotationMirror? =
        element.annotationMirrors.firstOrNull { mirror ->
            val type = mirror.annotationType.asElement() as? TypeElement
            type?.qualifiedName?.toString() == "net.adarw.rpc.RPCEndpoint"
        }

    private fun resolveAnnotationType(
        annotation: AnnotationMirror?,
        name: String,
    ): TypeMirror? {
        if (annotation == null) return null
        val valuesMap = processingEnv.elementUtils.getElementValuesWithDefaults(annotation)
        val entry =
            valuesMap.entries.firstOrNull { it.key.simpleName.toString() == name }
                ?: return null
        return entry.value.value as? TypeMirror
    }

    private fun resolveAnnotationSerializationType(
        annotation: AnnotationMirror?,
        name: String,
    ): Boolean? {
        if (annotation == null) return null
        val valuesMap = processingEnv.elementUtils.getElementValuesWithDefaults(annotation)
        val entry =
            valuesMap.entries.firstOrNull { it.key.simpleName.toString() == name }
                ?: return null
        return entry.value.value as? Boolean
    }

    private fun isSerializable(typeMirror: TypeMirror): Boolean {
        if (typeMirror.kind == TypeKind.VOID) return true
        if (typeMirror.kind.isPrimitive) return true
        val typeName = typeMirror.toString()
        if (typeName == "kotlin.Unit" || typeName == "java.lang.Void") return true
        if (typeName == "kotlin.String" || typeName == "java.lang.String") return true
        val element = processingEnv.typeUtils.asElement(typeMirror) as? TypeElement ?: return false
        return element.annotationMirrors.any { mirror ->
            val annotationType = mirror.annotationType.asElement() as? TypeElement
            annotationType?.qualifiedName?.toString() == "kotlinx.serialization.Serializable"
        }
    }

    private fun renderRegistry(endpoints: List<Endpoint>): String {
        val builder = StringBuilder()
        builder.appendLine("package net.adarw.rpc")
        builder.appendLine()
        builder.appendLine("import kotlin.reflect.KFunction")

        endpoints.mapNotNull { endpoint ->
            if (endpoint.packageName.isEmpty()) return@mapNotNull null
            if (endpoint.isTopLevel) {
                "${endpoint.packageName}.${endpoint.functionName}"
            } else {
                "${endpoint.packageName}.${endpoint.className}"
            }
        }
            .distinct()
            .sorted()
            .forEach {
                builder.appendLine("import $it")
            }

        builder.appendLine()
        builder.appendLine("object RPCRegistry {")

        if (endpoints.isEmpty()) {
            builder.appendLine("    fun registerAll() {}")
        } else {
            builder.appendLine("    fun registerAll() {")
            endpoints.forEach { endpoint ->
                val reference = if (endpoint.isTopLevel) {
                    "::${endpoint.functionName}"
                } else {
                    "${endpoint.className}::${endpoint.functionName}"
                }

                // Format type mirrors as Kotlin class references
                val inTypeRender = endpoint.inputType?.let { "$it::class" } ?: "null"
                val outTypeRender = endpoint.outputType?.let { "$it::class" } ?: "null"

                builder.appendLine("        RPCHandler.register(\"${endpoint.topic}\", $reference, $inTypeRender, $outTypeRender, ${endpoint.isProtobuf})")
            }
            builder.appendLine("    }")
        }
        builder.appendLine("}")
        return builder.toString()
    }

    private data class Endpoint(
        val packageName: String,
        val className: String,
        val functionName: String,
        val topic: String,
        val isTopLevel: Boolean,
        val isProtobuf: Boolean,
        val inputType: String?,
        val outputType: String?
    )
}