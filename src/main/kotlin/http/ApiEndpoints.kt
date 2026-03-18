package net.adarw.http

import net.adarw.rpc.definitions.StatusCode
import net.adarw.serialization.AppSerialization
import net.adarw.store.rpc.RPCStoreManager

fun loadDefinitions() = Unit

const val TBD = "Not Implemented Yet"

val fetchDevices =
    HttpEndpoint {
            it.requireMethod(HttpMethod.GET)
            it.sendResponse(
                StatusCode.OK,
                AppSerialization.json.encodeToString(
                    RPCStoreManager.getAllNodes()
                ),
            )
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
            it.requireMethod(HttpMethod.PUT)
            val json = it.getJsonPayload()
            val id = json.getInt("id")
            val config = json.getString("config")

            require(RPCStoreManager.idExists(id)) {
                "Id $id doesn't exists in RPC store, Try registering it first."
            }

            RPCStoreManager.setRawConfig(id, config)

            it.sendOk()
        }
        .registerEndpoint("/api/devices/updateConfig")

val deleteDevice =
    HttpEndpoint {
            it.requireMethod(HttpMethod.DELETE)
            val id = it.getJsonPayload().getInt("id")
            require(RPCStoreManager.idExists(id)) {
                "Id $id doesn't exists in RPC store, Try registering it first."
            }
            require(RPCStoreManager.delete(id)) {
                "Deletion has failed for id $id"
            }
            it.sendOk()
        }
        .registerEndpoint("/api/devices/delete")
