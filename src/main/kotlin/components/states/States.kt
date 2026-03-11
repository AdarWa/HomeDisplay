package net.adarw.components.states

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class State {
    abstract val definition: StateDefinition
    abstract val value: StateValue
}

@Serializable
@SerialName("hook")
data class HookState(
    override val definition: HookStateDefinition,
    override val value: StateValue,
) : State()

@Serializable
@SerialName("internal")
data class InternalState(
    override val definition: InternalStateDefinition,
    override val value: StateValue,
) : State()

fun StateDefinition.asState(value: StateValue) =
    when (this) {
        is InternalStateDefinition -> InternalState(this, value)
        is HookStateDefinition -> HookState(this, value)
    }
