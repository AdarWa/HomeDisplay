package net.adarw.hass.states

import net.adarw.components.states.InternalState

object LocalStateProvider : StateProvider {
    override fun getState(entityId: String): HAState {
        TODO("Not yet implemented")
    }

    override fun getInternalState(internalStateId: String): InternalState {
        TODO("Not yet implemented")
    }
}
