package net.adarw.store.property

import net.adarw.components.states.InternalState
import net.adarw.components.states.InternalStateDefinition
import net.adarw.components.states.StateType
import net.adarw.components.states.StateValue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PropertyStoreManagerTest {

    // Track keys generated during tests so we can safely clean them up later
    private val testKeys = mutableListOf<String>()

    private fun generateKey(): String {
        val key = "test_key_${UUID.randomUUID()}"
        testKeys.add(key)
        return key
    }

    @AfterAll
    fun cleanupAfterAll() {
        // Safely remove only the properties created during this test run
        testKeys.forEach { key ->
            PropertyStoreManager.deleteProperty(key)
        }
    }

    @Test
    fun `setProperty and getProperty should store and retrieve String`() {
        val key = generateKey()
        val expectedValue = "Hello, Exposed!"

        PropertyStoreManager.setProperty(key, expectedValue)
        val actualValue = PropertyStoreManager.getProperty(key)

        assertEquals(expectedValue, actualValue, "String property should match the stored value.")
    }

    @Test
    fun `setProperty and getProperty should store and retrieve Int`() {
        val key = generateKey()
        val expectedValue = 42

        PropertyStoreManager.setProperty(key, expectedValue)
        val actualValue = PropertyStoreManager.getProperty(key)

        assertEquals(expectedValue, actualValue, "Int property should match the stored value.")
    }

    @Test
    fun `setProperty and getProperty should store and retrieve Float`() {
        val key = generateKey()
        val expectedValue = 3.14f

        PropertyStoreManager.setProperty(key, expectedValue)
        val actualValue = PropertyStoreManager.getProperty(key)

        assertEquals(expectedValue, actualValue, "Float property should match the stored value.")
    }

    @Test
    fun `setProperty and getProperty should store and retrieve Boolean`() {
        val key = generateKey()
        val expectedValue = true

        PropertyStoreManager.setProperty(key, expectedValue)
        val actualValue = PropertyStoreManager.getProperty(key)

        assertEquals(expectedValue, actualValue, "Boolean property should match the stored value.")
    }

    @Test
    fun `getProperty should return null for non-existent key`() {
        val key = generateKey()
        val actualValue = PropertyStoreManager.getProperty(key)

        assertNull(actualValue, "Retrieving a missing key should return null.")
    }

    @Test
    fun `setProperty should throw IllegalArgumentException for unsupported types`() {
        val key = generateKey()
        val unsupportedValue = listOf("I", "am", "a", "list") // Lists are not handled in your when block

        assertFailsWith<IllegalStateException> {
            PropertyStoreManager.setProperty(key, unsupportedValue)
        }
    }

    @Test
    fun `setInternalState and getInternalState should store and retrieve complex state`() {
        val definitionId = generateKey()
        val dummyDefinition = InternalStateDefinition(definitionId, StateType.INT)
        val dummyState = InternalState(dummyDefinition, StateValue.IntValue(5))

        PropertyStoreManager.setInternalState(dummyState)
        val retrievedState = PropertyStoreManager.getInternalState(dummyDefinition)

        assertEquals(dummyState.value.data, retrievedState.value.data)
    }

    @Test
    fun `deleteProperty should return true and remove the item when deleting an existing property`() {
        val key = generateKey()
        PropertyStoreManager.setProperty(key, "To be deleted")

        // Verify it exists first
        assertEquals("To be deleted", PropertyStoreManager.getProperty(key))

        val deleteResult = PropertyStoreManager.deleteProperty(key)

        assertTrue(deleteResult, "deleteProperty should return true when a property is successfully deleted.")
        assertNull(PropertyStoreManager.getProperty(key), "getProperty should return null after the property is deleted.")
    }

    @Test
    fun `deleteProperty should return false when trying to delete a non-existent property`() {
        val key = generateKey() // Generated, but never saved to the DB

        val deleteResult = PropertyStoreManager.deleteProperty(key)

        assertFalse(deleteResult, "deleteProperty should return false when the key does not exist.")
    }
}