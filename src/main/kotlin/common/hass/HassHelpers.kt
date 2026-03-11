package net.adarw.common.hass

val entityIdRegex = "^[a-z0-9_]+\\.[a-z0-9_]+$".toRegex()

fun isValidEntityId(entityId: String): Boolean = entityIdRegex.matches(entityId)
