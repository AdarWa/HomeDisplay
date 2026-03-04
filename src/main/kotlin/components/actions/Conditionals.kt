package net.adarw.components.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.adarw.components.states.StateDefinition
import net.adarw.components.states.StateType
import net.adarw.components.states.StateValue
import net.adarw.hass.states.StateManager

@Serializable
enum class ComparisonOperator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    GREATER_OR_EQUAL,
    LESS_THAN,
    LESS_OR_EQUAL
}

@Serializable
sealed class Condition {
    abstract fun evaluate(): Boolean
}

@Serializable
@SerialName("stateComparison")
data class StateComparisonCondition(
    val state: StateDefinition,
    val operator: ComparisonOperator,
    val value: StateValue
) : Condition() {
    init {
        require(state.dataType == StateType.INT || state.dataType == StateType.FLOAT ||
                state.dataType == StateType.STRING || state.dataType == StateType.BOOLEAN
        ) { "Unsupported state type ${state.dataType}" }
        require(
            (state.dataType == StateType.INT && value is StateValue.IntValue) ||
                    (state.dataType == StateType.FLOAT && value is StateValue.FloatValue) ||
                    (state.dataType == StateType.STRING && value is StateValue.StringValue) ||
                    (state.dataType == StateType.BOOLEAN && value is StateValue.BooleanValue)
        ) { "Comparison value must match state type ${state.dataType}" }
        if (state.dataType == StateType.STRING || state.dataType == StateType.BOOLEAN) {
            require(operator == ComparisonOperator.EQUALS || operator == ComparisonOperator.NOT_EQUALS) {
                "Only equality comparisons are supported for ${state.dataType}"
            }
        }
    }

    override fun evaluate(): Boolean {
        val current = StateManager.getState(state).value
        return when (current) {
            is StateValue.IntValue -> compare(current.data, (value as StateValue.IntValue).data)
            is StateValue.FloatValue -> compare(current.data, (value as StateValue.FloatValue).data)
            is StateValue.StringValue -> compare(current.data, (value as StateValue.StringValue).data)
            is StateValue.BooleanValue -> compare(current.data, (value as StateValue.BooleanValue).data)
        }
    }

    private fun <T : Comparable<T>> compare(left: T, right: T): Boolean = when (operator) {
        ComparisonOperator.EQUALS -> left == right
        ComparisonOperator.NOT_EQUALS -> left != right
        ComparisonOperator.GREATER_THAN -> left > right
        ComparisonOperator.GREATER_OR_EQUAL -> left >= right
        ComparisonOperator.LESS_THAN -> left < right
        ComparisonOperator.LESS_OR_EQUAL -> left <= right
    }
}

@Serializable
@SerialName("stateRange")
data class StateRangeCondition(
    val state: StateDefinition,
    val min: StateValue? = null,
    val max: StateValue? = null,
    val inclusive: Boolean = true
) : Condition() {
    init {
        require(state.dataType == StateType.INT || state.dataType == StateType.FLOAT) {
            "Range condition requires int or float state, got ${state.dataType}"
        }
        require(min != null || max != null) { "Range condition requires min or max" }
        require(
            min == null ||
                (state.dataType == StateType.INT && min is StateValue.IntValue) ||
                (state.dataType == StateType.FLOAT && min is StateValue.FloatValue)
        ) { "Range min must match state type ${state.dataType}" }
        require(
            max == null ||
                (state.dataType == StateType.INT && max is StateValue.IntValue) ||
                (state.dataType == StateType.FLOAT && max is StateValue.FloatValue)
        ) { "Range max must match state type ${state.dataType}" }
    }

    override fun evaluate(): Boolean {
        val current = StateManager.getState(state).value
        return when (current) {
            is StateValue.IntValue -> checkBounds(current.data, min as StateValue.IntValue?, max as StateValue.IntValue?)
            is StateValue.FloatValue -> checkBounds(current.data, min as StateValue.FloatValue?, max as StateValue.FloatValue?)
            else -> error("Range condition requires int or float state, got ${state.dataType}")
        }
    }

    private fun <T : Comparable<T>> checkBounds(value: T, minValue: StateValue?, maxValue: StateValue?): Boolean {
        val minComparable = when (minValue) {
            is StateValue.IntValue -> minValue.data as T
            is StateValue.FloatValue -> minValue.data as T
            else -> null
        }
        val maxComparable = when (maxValue) {
            is StateValue.IntValue -> maxValue.data as T
            is StateValue.FloatValue -> maxValue.data as T
            else -> null
        }

        if (minComparable != null) {
            val minOk = if (inclusive) value >= minComparable else value > minComparable
            if (!minOk) return false
        }
        if (maxComparable != null) {
            val maxOk = if (inclusive) value <= maxComparable else value < maxComparable
            if (!maxOk) return false
        }
        return true
    }
}

@Serializable
@SerialName("and")
data class AndCondition(
    val conditions: List<Condition>
) : Condition() {
    override fun evaluate(): Boolean = conditions.all { it.evaluate() }
}

@Serializable
@SerialName("or")
data class OrCondition(
    val conditions: List<Condition>
) : Condition() {
    override fun evaluate(): Boolean = conditions.any { it.evaluate() }
}

@Serializable
@SerialName("not")
data class NotCondition(
    val condition: Condition
) : Condition() {
    override fun evaluate(): Boolean = !condition.evaluate()
}

@Serializable
@SerialName("conditional")
data class ConditionalAction(
    val condition: Condition,
    val ifTrue: Action? = null,
    val ifFalse: Action? = null
) : Action() {
    init {
        require(!(ifTrue == null && ifFalse == null)) { "Both actions of a ConditionalAction cannot be null" }
    }

    override fun perform() {
        if (condition.evaluate()) {
            ifTrue?.perform()
        } else {
            ifFalse?.perform()
        }
    }
}
