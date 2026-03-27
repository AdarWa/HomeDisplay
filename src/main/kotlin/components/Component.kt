package net.adarw.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.adarw.components.actions.NamedAction
import net.adarw.components.states.StateDefinition
import net.adarw.serialization.definitions.LvObject

@Serializable sealed class Component

@Serializable
@SerialName("namedComponent")
data class NamedComponent(val name: String) : Component()

@Serializable
@SerialName("definedComponent")
data class DefinedComponent(
    val internalStates: List<StateDefinition>,
    val actions: List<NamedAction>,
    val definition: LvObject,
    val name: String? = null,
) : Component()
