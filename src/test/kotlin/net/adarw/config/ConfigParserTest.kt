package net.adarw.config

import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigParserTest {

    @Test
    fun `Test ConfigParser encoding using default config`() {
        println(encodeConfig(defaultConfig()))
        assertEquals(
            encodeConfig(defaultConfig()),
            encodeConfig(parseConfig(encodeConfig(defaultConfig()))),
        )
    }

    @Test
    fun `Test ConfigParser encoding simplified config using default config`() {
        println("Simplified Config:")
        println(encodeSimplifiedConfig(defaultConfig().toSimplifiedConfig()))
    }
}
