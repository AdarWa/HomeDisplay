package net.adarw.config

import java.net.URI
import kotlin.text.isEmpty
import kotlin.text.split

data class Credentials(
    val user: String?,
    val password: String?,
    val isAuthorized: Boolean,
) {
    companion object {
        fun fromAuthorityString(authority: String?): Credentials {
            if (authority != null) {
                val splitted = authority.split(":")
                require(
                    (splitted.size == 2 &&
                        !splitted[0].isEmpty() &&
                        !splitted[1].isEmpty()) ||
                        (splitted.size == 1 && !splitted[0].isEmpty())
                ) {
                    "MQTT authority string must follow the following pattern 'user:password'"
                }
                return Credentials(
                    splitted.getOrElse(0, { "" }),
                    splitted.getOrElse(1, { "" }),
                    true,
                )
            }
            return Credentials(null, null, false)
        }
    }
}

object Environment {
    private val connectionUri: URI =
        URI.create(
                System.getenv("MQTT_CONNECT_URI").apply {
                    requireNotNull(this) {
                        "You must provide an MQTT connection URI"
                    }
                }
            )
            .apply {
                require(scheme == "mqtt") { "Connection protocol must be mqtt" }
            }

    val MQTT_BROKER = connectionUri.host
    val MQTT_PORT = connectionUri.port
    val MQTT_AUTH = Credentials.fromAuthorityString(connectionUri.userInfo)
    val MQTT_CLIENT_ID: String =
        System.getenv("MQTT_CLIENT_ID") ?: "HomeDisplay"
}
