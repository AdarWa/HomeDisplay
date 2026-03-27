package net.adarw.components.actions

import kotlinx.serialization.Serializable

@Serializable data class NamedAction(val name: String, val action: Action)
