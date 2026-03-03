package net.adarw.components.states

import net.adarw.serialization.AppSerialization
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StateTest {

    private val format = AppSerialization.json

    @Test
    fun `HookState should compute internalId correctly`() {
        val state = HookState(entityId = "light.living_room", dataType = StateType.INT)
        assertEquals("light.living_room", state.internalId)
    }

    @Test
    fun `HookState should throw exception for invalid entityId`() {
        assertThrows<IllegalArgumentException> {
            HookState(entityId = "invalid-id", dataType = StateType.STRING)
        }
    }

    @Test
    fun `InternalState should compute internalId correctly`() {
        val state = InternalState(id = "custom_id", dataType = StateType.BOOLEAN)
        println(state.internalId)
        assertEquals("custom_id", state.internalId)
    }

    @Test
    fun `HookState should serialize and deserialize correctly`() {
        val state: State = HookState(entityId = "switch.kitchen", dataType = StateType.BOOLEAN)
        val json = format.encodeToString(state)
        println(json)
        assertTrue(json.contains(""""type": "hook""""))

        val decoded = format.decodeFromString<State>(json)
        assertEquals(state, decoded)
    }

    @Test
    fun `InternalState should serialize and deserialize correctly`() {
        val state: State = InternalState(id = "local_01", dataType = StateType.STRING)
        val json = format.encodeToString(state)
        println(json)
        assertTrue(json.contains(""""type": "internal""""))

        val decoded = format.decodeFromString<State>(json)
        assertEquals(state, decoded)
    }
}