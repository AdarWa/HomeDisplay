package net.adarw.config

import kotlinx.serialization.Serializable
import net.adarw.components.DefinedComponent
import net.adarw.components.NamedComponent
import net.adarw.components.Screen
import net.adarw.components.actions.NamedAction
import net.adarw.components.states.StateDefinition
import net.mamoe.yamlkt.Yaml

@Serializable
data class Config(
    val internalStates: List<StateDefinition>,
    val actions: List<NamedAction>,
    val definedComponents: List<DefinedComponent>,
    val screens: List<Screen>,
)

@Serializable
data class SimplifiedConfig(
    val internalStates: List<StateDefinition>,
    val actions: List<NamedAction>,
    val screens: List<Screen>,
)

fun parseConfig(config: String) =
    Yaml.decodeFromString(Config.serializer(), config)

fun parseSimplifiedConfig(config: String): SimplifiedConfig =
    parseConfig(config).toSimplifiedConfig()

fun encodeSimplifiedConfig(config: SimplifiedConfig) =
    Yaml.encodeToString(SimplifiedConfig.serializer(), config)

fun Config.toSimplifiedConfig(): SimplifiedConfig {
    val componentDefinitions =
        definedComponents.filter { it.name != null }.associateBy { it.name!! }

    val resolvedScreens =
        screens.map { screen ->
            val resolvedComponents =
                screen.components.map { component ->
                    when (component) {
                        is NamedComponent ->
                            componentDefinitions[component.name]
                                ?: error(
                                    "No defined component found for name '${component.name}'"
                                )
                        is DefinedComponent -> component
                    }
                }
            screen.copy(components = resolvedComponents)
        }

    return SimplifiedConfig(
        internalStates = internalStates,
        actions = actions,
        screens = resolvedScreens,
    )
}

fun encodeConfig(config: Config) =
    Yaml.encodeToString(Config.serializer(), config)
