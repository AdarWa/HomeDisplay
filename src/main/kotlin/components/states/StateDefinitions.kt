package net.adarw.components.states

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.adarw.common.hass.isValidEntityId
import net.adarw.hass.states.StateManager

@Serializable
sealed class StateDefinition {
    abstract val dataType: StateType

    internal val internalId: String
        get() = computeInternalId()

    abstract fun fetchLatestValue(): StateValue

    protected abstract fun computeInternalId(): String
}

@Serializable
@SerialName("hook")
data class HookStateDefinition(
    val entityId: String,
    override val dataType: StateType,
) : StateDefinition() {
    init {
        require(isValidEntityId(entityId)) {
            "entityId must be a valid Home Assistant id, got $entityId"
        }
    }

    override fun computeInternalId(): String = entityId

    override fun fetchLatestValue() =
        StateManager.getHAState(entityId).state.toStateValue()
}

@Serializable
@SerialName("internal")
data class InternalStateDefinition(
    val id: String,
    override val dataType: StateType,
) : StateDefinition() {
    override fun computeInternalId(): String = id

    override fun fetchLatestValue() =
        StateManager.getInternalState(computeInternalId()).value
}
