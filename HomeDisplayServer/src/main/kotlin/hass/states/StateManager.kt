package net.adarw.hass.states

import net.adarw.STATE_PROVIDER
import net.adarw.components.states.InternalState
import net.adarw.components.states.State
import net.adarw.components.states.StateDefinition
import net.adarw.components.states.StateValue

object StateManager {

    private val stateProvider = STATE_PROVIDER

    fun getHAState(entityId: String): HAState = stateProvider.getState(entityId)

    fun getInternalState(internalStateId: String): InternalState = stateProvider.getInternalState(internalStateId)

    fun getState(state: StateDefinition): State = TODO()

    fun setState(state: StateDefinition, value: StateValue): Unit = TODO()
}