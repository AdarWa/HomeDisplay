package net.adarw.serialization

import io.github.classgraph.ClassGraph

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RegisterPolymorphic

fun autoRegisterSerialization() {
    ClassGraph()
        .enableMethodInfo()
        .enableAnnotationInfo()
        .acceptPackages("net.adarw") // Keep this constrained for speed
        .scan().use { scanResult ->

            val annotationName = RegisterPolymorphic::class.java.name

            val classes = scanResult.getClassesWithMethodAnnotation(annotationName)

            for (classInfo in classes) {
                val annotatedMethods = classInfo.methodInfo.filter { methodInfo ->
                    methodInfo.hasAnnotation(annotationName)
                }

                for (methodInfo in annotatedMethods) {
                    val method = methodInfo.loadClassAndGetMethod()

                    method.invoke(null)
                }
            }
        }
}