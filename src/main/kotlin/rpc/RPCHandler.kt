package net.adarw.rpc

import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.MQTTVersion
import io.github.davidepianca98.mqtt.Subscription
import io.github.davidepianca98.mqtt.packets.mqtt.MQTTPublish
import kotlin.reflect.KFunction
import mu.KotlinLogging
import net.adarw.config.Environment
import kotlin.reflect.KClass

data class Endpoint(
    val function: KFunction<*>,
    val inputType: KClass<*>,
    val outputType: KClass<*>,
    val isProtobuf: Boolean
)

object RPCHandler {
    private val logger = KotlinLogging.logger {}
    private val endpoints = mutableMapOf<String, Endpoint>()

    @OptIn(ExperimentalUnsignedTypes::class)
    private val client =
        MQTTClient(
            MQTTVersion.MQTT5,
            Environment.MQTT_BROKER,
            Environment.MQTT_PORT,
            clientId = Environment.MQTT_CLIENT_ID,
            userName = Environment.MQTT_AUTH.user,
            password =
                Environment.MQTT_AUTH.password
                    ?.encodeToByteArray()
                    ?.toUByteArray(),
            tls = null,
        ) { publish ->
            handleMessage(publish)
        }

    init {
        RPCRegistry.registerAll()
        subscribeAll()
    }

    fun register(topic: String, endpoint: KFunction<*>, inputType: KClass<*>, outputType: KClass<*>, isProtobuf: Boolean) {
        endpoints[topic] = Endpoint(endpoint, inputType, outputType, isProtobuf)
    }

    fun registeredEndpoints(): Map<String, Triple<KFunction<*>, KClass<*>, KClass<*>>> = endpoints.toMap()

    private fun subscribeAll(){
        client.subscribe(listOf(
            Subscription(
                "HomeDisplay/rpc/#"
            )
        ))
    }

    private fun handleMessage(publish: MQTTPublish){
        logger.info { "Received RPC message from topic ${publish.topicName}" }
        if(!endpoints.containsKey(publish.topicName)){
            logger.warn { "Received a message not on a registered endpoint ${publish.topicName}" }
            return
        }
        publish.payload
        endpoints[publish.topicName]?.call()
    }
}
