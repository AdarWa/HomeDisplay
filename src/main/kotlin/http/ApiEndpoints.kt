package net.adarw.http

import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.adarw.rpc.definitions.StatusCode
import net.adarw.store.rpc.RPCStoreManager

fun loadDefinitions() = Unit

const val TBD = "Not Implemented Yet"

val fetchDevices =
    HttpEndpoint {
            it.requireMethod(HttpMethod.GET)
            it.sendResponse(StatusCode.OK, TBD)
        }
        .registerEndpoint("/api/devices/fetch")

val pingDevice =
    HttpEndpoint {
            it.requireMethod(HttpMethod.POST)
            it.sendResponse(StatusCode.OK, TBD)
        }
        .registerEndpoint("/api/devices/ping")

val syncDevice =
    HttpEndpoint {
            it.requireMethod(HttpMethod.POST)
            it.sendResponse(StatusCode.OK, TBD)
        }
        .registerEndpoint("/api/devices/sync")

val rebootDevice =
    HttpEndpoint {
            it.requireMethod(HttpMethod.POST)
            it.sendResponse(StatusCode.OK, TBD)
        }
        .registerEndpoint("/api/devices/reboot")

val updateDeviceConfig =
    HttpEndpoint {
            it.requireMethod(HttpMethod.POST)
            val json = it.getJsonPayload()
            val id =
                json.jsonObject["id"]?.jsonPrimitive?.content?.toInt()
                    ?: error("No id present in update config request")
            val config =
                json.jsonObject["config"]?.jsonPrimitive?.content
                    ?: error("No config present in update config request")

            require(RPCStoreManager.idExists(id)) {
                "Id $id doesn't exists in RPC store, Try registering it first."
            }

            RPCStoreManager.setRawConfig(id, config)

            it.sendResponse(StatusCode.OK, "OK")
        }
        .registerEndpoint("/api/devices/updateDeviceConfig")

val deleteDevice =
    HttpEndpoint {
            it.requireMethod(HttpMethod.DELETE)
            it.sendResponse(StatusCode.OK, TBD)
        }
        .registerEndpoint("/api/devices/deleteDevice")
