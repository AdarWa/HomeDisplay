package net.adarw.serialization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Disabled

interface TestWidget

@Serializable
@SerialName("SmartWidget")
data class SmartWidget(val version: Int) : TestWidget

@Disabled("Not stable for now")
class AutoRegistrationTest {
    @Test
    fun `test autoRegisterSerialization executes annotated functions successfully`() {
        AppSerialization.register<TestWidget, SmartWidget>()

        val originalWidget: TestWidget = SmartWidget(version = 42)

        val jsonString = AppSerialization.json.encodeToString(originalWidget)
        println(jsonString)
        assertTrue(
            jsonString.contains(""""type": "SmartWidget""""),
            "Expected discriminator 'SmartWidget' not found in JSON: $jsonString",
        )

        val decodedWidget =
            AppSerialization.json.decodeFromString<TestWidget>(jsonString)
        assertEquals(
            originalWidget,
            decodedWidget,
            "Decoded object does not match the original",
        )
    }
}
