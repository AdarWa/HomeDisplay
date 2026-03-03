package net.adarw.components.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.adarw.components.states.StateDefinition
import net.adarw.components.states.StateType
import net.adarw.components.states.StateValue
import net.adarw.hass.states.StateManager

@Serializable
sealed class Action {
    abstract fun perform()
}

@Serializable
@SerialName("setValue")
data class SetValueAction(
    val state: StateDefinition,
    val value: StateValue
) : Action() {
    override fun perform() {
        StateManager.setState(state, value)
    }
}

@Serializable
@SerialName("toggleValue")
data class ToggleValueAction(
    val state: StateDefinition
) : Action() {
    init {
        require(state.dataType == StateType.BOOLEAN) { "Toggleable state must be of type boolean, got ${state.dataType}" }
    }

    override fun perform() {
        val curr = StateManager.getState(state).value as StateValue.BooleanValue
        StateManager.setState(state, StateValue.BooleanValue(!curr.data))
    }
}
