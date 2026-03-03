package net.adarw.hass.states

import net.adarw.STATE_PROVIDER
import net.adarw.components.states.InternalState

object StateManager {

    private val stateProvider = STATE_PROVIDER

    fun getHAState(entityId: String): HAState = stateProvider.getState(entityId)

    fun getInternalState(internalStateId: String): InternalState = stateProvider.getInternalState(internalStateId)
}