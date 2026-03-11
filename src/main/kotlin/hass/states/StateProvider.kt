package net.adarw.hass.states

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.adarw.components.states.InternalState

@Serializable
data class HAState(
    @SerialName("entity_id") val entityId: String,
    val state: String,
    val attributes: Map<String, @Contextual Any?>,
    @SerialName("last_changed") val lastChanged: String,
    @SerialName("last_updated") val lastUpdated: String,
    val context: HAContext,
)

@Serializable
data class HAContext(
    val id: String,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("user_id") val userId: String? = null,
)

interface StateProvider {
    fun getState(entityId: String): HAState

    fun getInternalState(internalStateId: String): InternalState
}
