package net.adarw.config

import net.adarw.components.DefinedComponent
import net.adarw.components.NamedComponent
import net.adarw.components.Screen
import net.adarw.components.actions.DecrementValueAction
import net.adarw.components.actions.IncrementValueAction
import net.adarw.components.actions.NamedAction
import net.adarw.components.actions.ToggleValueAction
import net.adarw.components.states.HookStateDefinition
import net.adarw.components.states.InternalStateDefinition
import net.adarw.components.states.StateType
import net.adarw.components.states.StateValue
import net.adarw.serialization.definitions.EventHandler
import net.adarw.serialization.definitions.EventType
import net.adarw.serialization.definitions.LvObject
import net.adarw.serialization.definitions.WidgetType

fun defaultConfig(): Config {
    val livingRoomLight =
        HookStateDefinition("light.living_room", StateType.BOOLEAN)
    val kitchenLight = HookStateDefinition("light.kitchen", StateType.BOOLEAN)
    val livingRoomTemp =
        HookStateDefinition("sensor.living_room_temperature", StateType.FLOAT)
    val livingRoomHumidity =
        HookStateDefinition("sensor.living_room_humidity", StateType.FLOAT)
    val temperatureSetpoint =
        InternalStateDefinition("temperature_setpoint", StateType.FLOAT)
    val sleepMode = InternalStateDefinition("sleep_mode", StateType.BOOLEAN)
    val displayBrightness =
        InternalStateDefinition("display_brightness", StateType.INT)

    val actions =
        listOf(
            NamedAction(
                "toggle_living_room_light",
                ToggleValueAction(livingRoomLight),
            ),
            NamedAction("toggle_kitchen_light", ToggleValueAction(kitchenLight)),
            NamedAction(
                "increase_setpoint",
                IncrementValueAction(
                    temperatureSetpoint,
                    StateValue.FloatValue(0.5f),
                ),
            ),
            NamedAction(
                "decrease_setpoint",
                DecrementValueAction(
                    temperatureSetpoint,
                    StateValue.FloatValue(0.5f),
                ),
            ),
            NamedAction("toggle_sleep_mode", ToggleValueAction(sleepMode)),
            NamedAction(
                "increase_brightness",
                IncrementValueAction(
                    displayBrightness,
                    StateValue.IntValue(10),
                ),
            ),
            NamedAction(
                "decrease_brightness",
                DecrementValueAction(
                    displayBrightness,
                    StateValue.IntValue(10),
                ),
            ),
        )

    val headerComponent =
        DefinedComponent(
            name = "header",
            internalStates = emptyList(),
            actions = emptyList(),
            definition =
                LvObject(
                    type = WidgetType.OBJ,
                    id = "header",
                    x = 0,
                    y = 0,
                    width = 240,
                    height = 32,
                    children =
                        listOf(
                            LvObject(
                                type = WidgetType.LABEL,
                                id = "header_title",
                                x = 8,
                                y = 8,
                                text = "Home Display",
                            )
                        ),
                ),
        )

    val statusPanel =
        DefinedComponent(
            name = "status_panel",
            internalStates = emptyList(),
            actions = emptyList(),
            definition =
                LvObject(
                    type = WidgetType.OBJ,
                    id = "status_panel",
                    x = 0,
                    y = 36,
                    width = 240,
                    height = 52,
                    children =
                        listOf(
                            LvObject(
                                type = WidgetType.LABEL,
                                id = "status_title",
                                x = 8,
                                y = 4,
                                text = "Status",
                            ),
                            LvObject(
                                type = WidgetType.LABEL,
                                id = "status_temp",
                                x = 8,
                                y = 22,
                                text = "Temp: --°C",
                            ),
                            LvObject(
                                type = WidgetType.LABEL,
                                id = "status_humidity",
                                x = 120,
                                y = 22,
                                text = "Humidity: --%",
                            ),
                        ),
                ),
        )

    val lightsPanel =
        DefinedComponent(
            name = "lights_panel",
            internalStates = emptyList(),
            actions = emptyList(),
            definition =
                LvObject(
                    type = WidgetType.OBJ,
                    id = "lights_panel",
                    x = 0,
                    y = 92,
                    width = 240,
                    height = 88,
                    children =
                        listOf(
                            LvObject(
                                type = WidgetType.LABEL,
                                id = "lights_title",
                                x = 8,
                                y = 4,
                                text = "Lights",
                            ),
                            LvObject(
                                type = WidgetType.BUTTON,
                                id = "lights_living",
                                x = 8,
                                y = 24,
                                width = 104,
                                height = 28,
                                text = "Living Room",
                                events =
                                    listOf(
                                        EventHandler(
                                            trigger = EventType.CLICKED,
                                            actionId = "toggle_living_room_light",
                                        )
                                    ),
                            ),
                            LvObject(
                                type = WidgetType.BUTTON,
                                id = "lights_kitchen",
                                x = 128,
                                y = 24,
                                width = 104,
                                height = 28,
                                text = "Kitchen",
                                events =
                                    listOf(
                                        EventHandler(
                                            trigger = EventType.CLICKED,
                                            actionId = "toggle_kitchen_light",
                                        )
                                    ),
                            ),
                        ),
                ),
        )

    val climatePanel =
        DefinedComponent(
            name = "climate_panel",
            internalStates = emptyList(),
            actions = emptyList(),
            definition =
                LvObject(
                    type = WidgetType.OBJ,
                    id = "climate_panel",
                    x = 0,
                    y = 36,
                    width = 240,
                    height = 120,
                    children =
                        listOf(
                            LvObject(
                                type = WidgetType.LABEL,
                                id = "climate_title",
                                x = 8,
                                y = 4,
                                text = "Climate",
                            ),
                            LvObject(
                                type = WidgetType.LABEL,
                                id = "climate_setpoint",
                                x = 8,
                                y = 28,
                                text = "Setpoint: --°C",
                            ),
                            LvObject(
                                type = WidgetType.BUTTON,
                                id = "setpoint_down",
                                x = 8,
                                y = 56,
                                width = 104,
                                height = 28,
                                text = "-0.5°",
                                events =
                                    listOf(
                                        EventHandler(
                                            trigger = EventType.CLICKED,
                                            actionId = "decrease_setpoint",
                                        )
                                    ),
                            ),
                            LvObject(
                                type = WidgetType.BUTTON,
                                id = "setpoint_up",
                                x = 128,
                                y = 56,
                                width = 104,
                                height = 28,
                                text = "+0.5°",
                                events =
                                    listOf(
                                        EventHandler(
                                            trigger = EventType.CLICKED,
                                            actionId = "increase_setpoint",
                                        )
                                    ),
                            ),
                        ),
                ),
        )

    val systemPanel =
        DefinedComponent(
            name = "system_panel",
            internalStates = emptyList(),
            actions = emptyList(),
            definition =
                LvObject(
                    type = WidgetType.OBJ,
                    id = "system_panel",
                    x = 0,
                    y = 36,
                    width = 240,
                    height = 120,
                    children =
                        listOf(
                            LvObject(
                                type = WidgetType.LABEL,
                                id = "system_title",
                                x = 8,
                                y = 4,
                                text = "System",
                            ),
                            LvObject(
                                type = WidgetType.BUTTON,
                                id = "sleep_mode_toggle",
                                x = 8,
                                y = 28,
                                width = 216,
                                height = 28,
                                text = "Toggle Sleep Mode",
                                events =
                                    listOf(
                                        EventHandler(
                                            trigger = EventType.CLICKED,
                                            actionId = "toggle_sleep_mode",
                                        )
                                    ),
                            ),
                            LvObject(
                                type = WidgetType.BUTTON,
                                id = "brightness_down",
                                x = 8,
                                y = 64,
                                width = 104,
                                height = 28,
                                text = "Dim",
                                events =
                                    listOf(
                                        EventHandler(
                                            trigger = EventType.CLICKED,
                                            actionId = "decrease_brightness",
                                        )
                                    ),
                            ),
                            LvObject(
                                type = WidgetType.BUTTON,
                                id = "brightness_up",
                                x = 128,
                                y = 64,
                                width = 104,
                                height = 28,
                                text = "Brighten",
                                events =
                                    listOf(
                                        EventHandler(
                                            trigger = EventType.CLICKED,
                                            actionId = "increase_brightness",
                                        )
                                    ),
                            ),
                        ),
                ),
        )

    val screens =
        listOf(
            Screen(
                name = "Home",
                components =
                    listOf(
                        NamedComponent("header"),
                        NamedComponent("status_panel"),
                        NamedComponent("lights_panel"),
                    ),
            ),
            Screen(
                name = "Climate",
                components =
                    listOf(
                        NamedComponent("header"),
                        NamedComponent("climate_panel"),
                    ),
            ),
            Screen(
                name = "System",
                components =
                    listOf(
                        NamedComponent("header"),
                        NamedComponent("system_panel"),
                    ),
            ),
        )

    return Config(
        internalStates =
            listOf(
                livingRoomLight,
                kitchenLight,
                livingRoomTemp,
                livingRoomHumidity,
                temperatureSetpoint,
                sleepMode,
                displayBrightness,
            ),
        actions = actions,
        definedComponents =
            listOf(
                headerComponent,
                statusPanel,
                lightsPanel,
                climatePanel,
                systemPanel,
            ),
        screens = screens,
    )
}
