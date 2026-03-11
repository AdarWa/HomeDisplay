package net.adarw.serialization

import kotlin.reflect.KClass
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import net.adarw.common.decoders.AnySerializer

object AppSerialization {
    data class PolymorphicRegistration<Base : Any, Sub : Base>(
        val base: KClass<Base>,
        val subclass: KClass<Sub>,
        val serializer: KSerializer<Sub>,
    )

    val registrations = mutableListOf<PolymorphicRegistration<*, *>>()

    inline fun <reified Base : Any, reified Sub : Base> register() {
        registrations.add(
            PolymorphicRegistration(Base::class, Sub::class, serializer<Sub>())
        )
    }

    val json: Json by lazy {
        Json {
            serializersModule = SerializersModule {
                registrations.forEach { reg -> registerPolymorphic(reg) }
                contextual(AnySerializer)
            }
            classDiscriminator = "type"
            ignoreUnknownKeys = true
            coerceInputValues = true
            prettyPrint = true
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun SerializersModuleBuilder.registerPolymorphic(
        reg: PolymorphicRegistration<*, *>
    ) {
        val baseClass = reg.base as KClass<Any>
        val subClass = reg.subclass as KClass<Any>
        val serializer = reg.serializer as KSerializer<Any>

        polymorphic(baseClass) { subclass(subClass, serializer) }
    }
}
