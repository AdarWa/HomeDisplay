package net.adarw.components.states

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.adarw.common.hass.isValidEntityId
import net.adarw.hass.states.StateManager

@Serializable
sealed class State {
    abstract val dataType: StateType

    internal val internalId: String
        get() = computeInternalId()

    abstract fun fetchLatestValue(): StateValue

    protected abstract fun computeInternalId(): String
}

@Serializable
@SerialName("hook")
data class HookState(
    val entityId: String,
    override val dataType: StateType
) : State() {

    init {
        require(isValidEntityId(entityId))
    }


    override fun fetchLatestValue() = StateManager.getHAState(entityId).state.toStateValue()

    override fun computeInternalId(): String = entityId
}

@Serializable
@SerialName("internal")
data class InternalState(
    val id: String,
    override val dataType: StateType
) : State() {
    override fun computeInternalId(): String = id

    override fun fetchLatestValue() = StateManager.getInternalState(computeInternalId())
}