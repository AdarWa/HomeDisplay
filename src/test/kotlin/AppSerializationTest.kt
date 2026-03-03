package net.adarw

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import net.adarw.serialization.AppSerialization
import net.adarw.serialization.RegisterPolymorphic
import net.adarw.serialization.autoRegisterSerialization
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


interface TestWidget

@Serializable
@SerialName("SmartWidget")
data class SmartWidget(val version: Int) : TestWidget

@RegisterPolymorphic
fun registerTestWidgets() {
    AppSerialization.register<TestWidget, SmartWidget>()
}

class AutoRegistrationTest {

    @Test
    fun `test autoRegisterSerialization executes annotated functions successfully`() {
        autoRegisterSerialization()

        val originalWidget: TestWidget = SmartWidget(version = 42)

        val jsonString = AppSerialization.json.encodeToString(originalWidget)
        println(jsonString)
        assertTrue(
            jsonString.contains(""""type": "SmartWidget""""),
            "Expected discriminator 'SmartWidget' not found in JSON: $jsonString"
        )

        val decodedWidget = AppSerialization.json.decodeFromString<TestWidget>(jsonString)
        assertEquals(
            originalWidget,
            decodedWidget,
            "Decoded object does not match the original"
        )
    }
}