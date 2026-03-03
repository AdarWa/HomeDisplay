package net.adarw.components.states

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

enum class StateType(val dType: KClass<*>) {
    INT(Int::class),
    FLOAT(Float::class),
    STRING(String::class),
    BOOLEAN(Boolean::class)
}

@Serializable
sealed interface StateValue {
    @Serializable
    @SerialName("int")
    data class IntValue(val data: Int) : StateValue

    @Serializable
    @SerialName("float")
    data class FloatValue(val data: Float) : StateValue

    @Serializable
    @SerialName("string")
    data class StringValue(val data: String) : StateValue

    @Serializable
    @SerialName("boolean")
    data class BooleanValue(val data: Boolean) : StateValue
}

fun Int.toIntStateValue(): StateValue.IntValue = StateValue.IntValue(this)
fun Float.toFloatStateValue(): StateValue.FloatValue = StateValue.FloatValue(this)
fun String.toStringStateValue(): StateValue.StringValue = StateValue.StringValue(this)
fun Boolean.toBooleanStateValue(): StateValue.BooleanValue = StateValue.BooleanValue(this)

fun String.toStateValue(): StateValue {
    val lower = this.lowercase()
    if (lower == "on" || lower == "true") return StateValue.BooleanValue(true)
    if (lower == "off" || lower == "false") return StateValue.BooleanValue(false)

    val intValue = this.toIntOrNull()
    if (intValue != null) return StateValue.IntValue(intValue)

    val floatValue = this.toFloatOrNull()
    if (floatValue != null) return StateValue.FloatValue(floatValue)

    return StateValue.StringValue(this)
}