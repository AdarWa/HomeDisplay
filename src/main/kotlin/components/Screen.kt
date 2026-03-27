package net.adarw.components

import kotlinx.serialization.Serializable

@Serializable
data class Screen(
    val name: String,
    val components: List<Component>
)