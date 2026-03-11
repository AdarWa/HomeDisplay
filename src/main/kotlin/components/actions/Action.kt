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
data class SetValueAction(val state: StateDefinition, val value: StateValue) :
    Action() {
    override fun perform() {
        StateManager.setState(state, value)
    }
}

@Serializable
@SerialName("toggleValue")
data class ToggleValueAction(val state: StateDefinition) : Action() {
    init {
        require(state.dataType == StateType.BOOLEAN) {
            "Toggleable state must be of type boolean, got ${state.dataType}"
        }
    }

    override fun perform() {
        val curr = StateManager.getState(state).value as StateValue.BooleanValue
        StateManager.setState(state, StateValue.BooleanValue(!curr.data))
    }
}

@Serializable
@SerialName("incrementValue")
data class IncrementValueAction(
    val state: StateDefinition,
    val amount: StateValue,
) : Action() {
    init {
        require(
            state.dataType == StateType.INT || state.dataType == StateType.FLOAT
        ) {
            "Incrementable state must be of type int or float, got ${state.dataType}"
        }
        require(
            (state.dataType == StateType.INT &&
                amount is StateValue.IntValue) ||
                (state.dataType == StateType.FLOAT &&
                    amount is StateValue.FloatValue)
        ) {
            "Increment amount must match state type ${state.dataType}"
        }
    }

    override fun perform() {
        val current = StateManager.getState(state).value
        val newValue =
            when (current) {
                is StateValue.IntValue -> {
                    StateValue.IntValue(
                        current.data + (amount as StateValue.IntValue).data
                    )
                }

                is StateValue.FloatValue -> {
                    StateValue.FloatValue(
                        current.data + (amount as StateValue.FloatValue).data
                    )
                }

                else -> {
                    error(
                        "IncrementValueAction requires int or float state, got ${state.dataType}"
                    )
                }
            }
        StateManager.setState(state, newValue)
    }
}

@Serializable
@SerialName("decrementValue")
data class DecrementValueAction(
    val state: StateDefinition,
    val amount: StateValue,
) : Action() {
    init {
        require(
            state.dataType == StateType.INT || state.dataType == StateType.FLOAT
        ) {
            "Decrementable state must be of type int or float, got ${state.dataType}"
        }
        require(
            (state.dataType == StateType.INT &&
                amount is StateValue.IntValue) ||
                (state.dataType == StateType.FLOAT &&
                    amount is StateValue.FloatValue)
        ) {
            "Decrement amount must match state type ${state.dataType}"
        }
    }

    override fun perform() {
        val newValue =
            when (val current = StateManager.getState(state).value) {
                is StateValue.IntValue -> {
                    StateValue.IntValue(
                        current.data - (amount as StateValue.IntValue).data
                    )
                }

                is StateValue.FloatValue -> {
                    StateValue.FloatValue(
                        current.data - (amount as StateValue.FloatValue).data
                    )
                }

                else -> {
                    error(
                        "DecrementValueAction requires int or float state, got ${state.dataType}"
                    )
                }
            }
        StateManager.setState(state, newValue)
    }
}

@Serializable
@SerialName("multiplyValue")
data class MultiplyValueAction(
    val state: StateDefinition,
    val factor: StateValue,
) : Action() {
    init {
        require(
            state.dataType == StateType.INT || state.dataType == StateType.FLOAT
        ) {
            "Multipliable state must be of type int or float, got ${state.dataType}"
        }
        require(
            (state.dataType == StateType.INT &&
                factor is StateValue.IntValue) ||
                (state.dataType == StateType.FLOAT &&
                    factor is StateValue.FloatValue)
        ) {
            "Multiplication factor must match state type ${state.dataType}"
        }
    }

    override fun perform() {
        val newValue =
            when (val current = StateManager.getState(state).value) {
                is StateValue.IntValue -> {
                    StateValue.IntValue(
                        current.data * (factor as StateValue.IntValue).data
                    )
                }

                is StateValue.FloatValue -> {
                    StateValue.FloatValue(
                        current.data * (factor as StateValue.FloatValue).data
                    )
                }

                else -> {
                    error(
                        "MultiplyValueAction requires int or float state, got ${state.dataType}"
                    )
                }
            }
        StateManager.setState(state, newValue)
    }
}

@Serializable
@SerialName("composite")
data class CompositeAction(val actions: List<Action>) : Action() {
    override fun perform() {
        actions.forEach { it.perform() }
    }
}

@Serializable
@SerialName("delay")
data class DelayAction(val durationMs: Long) : Action() {
    init {
        require(durationMs >= 0) { "Delay duration must be non-negative" }
    }

    override fun perform() {
        Thread.sleep(durationMs)
    }
}

@Serializable
@SerialName("clampValue")
data class ClampValueAction(
    val state: StateDefinition,
    val min: StateValue? = null,
    val max: StateValue? = null,
) : Action() {
    init {
        require(
            state.dataType == StateType.INT || state.dataType == StateType.FLOAT
        ) {
            "Clampable state must be of type int or float, got ${state.dataType}"
        }
        require(min != null || max != null) { "Clamp requires min or max" }
        require(
            min == null ||
                (state.dataType == StateType.INT &&
                    min is StateValue.IntValue) ||
                (state.dataType == StateType.FLOAT &&
                    min is StateValue.FloatValue)
        ) {
            "Clamp min must match state type ${state.dataType}"
        }
        require(
            max == null ||
                (state.dataType == StateType.INT &&
                    max is StateValue.IntValue) ||
                (state.dataType == StateType.FLOAT &&
                    max is StateValue.FloatValue)
        ) {
            "Clamp max must match state type ${state.dataType}"
        }
    }

    override fun perform() {
        val clamped =
            when (val current = StateManager.getState(state).value) {
                is StateValue.IntValue -> clampInt(current)
                is StateValue.FloatValue -> clampFloat(current)
                else ->
                    error(
                        "ClampValueAction requires int or float state, got ${state.dataType}"
                    )
            }
        StateManager.setState(state, clamped)
    }

    private fun clampInt(current: StateValue.IntValue): StateValue.IntValue {
        val minValue = (min as? StateValue.IntValue)?.data
        val maxValue = (max as? StateValue.IntValue)?.data
        val value = current.data
        val clamped =
            when {
                minValue != null && value < minValue -> minValue
                maxValue != null && value > maxValue -> maxValue
                else -> value
            }
        return StateValue.IntValue(clamped)
    }

    private fun clampFloat(
        current: StateValue.FloatValue
    ): StateValue.FloatValue {
        val minValue = (min as? StateValue.FloatValue)?.data
        val maxValue = (max as? StateValue.FloatValue)?.data
        val value = current.data
        val clamped =
            when {
                minValue != null && value < minValue -> minValue
                maxValue != null && value > maxValue -> maxValue
                else -> value
            }
        return StateValue.FloatValue(clamped)
    }
}

@Serializable
@SerialName("copyValue")
data class CopyValueAction(
    val source: StateDefinition,
    val target: StateDefinition,
) : Action() {
    init {
        require(source.dataType == target.dataType) {
            "CopyValueAction requires matching types, got ${source.dataType} and ${target.dataType}"
        }
    }

    override fun perform() {
        val value = StateManager.getState(source).value
        StateManager.setState(target, value)
    }
}
