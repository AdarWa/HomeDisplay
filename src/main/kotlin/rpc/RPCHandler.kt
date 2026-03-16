package net.adarw.rpc

import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.MQTTVersion
import io.github.davidepianca98.mqtt.Subscription
import io.github.davidepianca98.mqtt.packets.Qos
import io.github.davidepianca98.mqtt.packets.mqtt.MQTTPublish
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlinx.serialization.ExperimentalSerializationApi
import mu.KotlinLogging
import net.adarw.config.Environment

data class Endpoint(
    val function: KFunction<*>,
    val inputType: KClass<*>,
    val outputType: KClass<*>,
    val isProtobuf: Boolean,
)

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalSerializationApi::class)
object RPCHandler {
    private val logger = KotlinLogging.logger {}
    private val endpoints = mutableMapOf<String, Endpoint>()

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
            try {
                handleMessage(publish)
            } catch (exception: Exception) {
                logger.error(exception) {
                    "Unhandled exception while processing RPC message"
                }
            }
        }

    private val thread =
        Thread {
                logger.info { "Starting MQTT5 client..." }
                client.run()
            }
            .apply { name = "mqtt" }

    init {
        thread.start()
        RPCRegistry.registerAll()
        subscribeAll()
    }

    fun register(
        topic: String,
        endpoint: KFunction<*>,
        inputType: KClass<*>,
        outputType: KClass<*>,
        isProtobuf: Boolean,
    ) {
        endpoints[topic] = Endpoint(endpoint, inputType, outputType, isProtobuf)
    }

    fun registeredEndpoints(): Map<String, Endpoint> = endpoints.toMap()

    private fun subscribeAll() {
        client.subscribe(listOf(Subscription("HomeDisplay/rpc/request/#")))
    }

    private fun publishMessage(
        topic: String,
        payload: UByteArray,
        checkDelivery: Boolean = false,
    ) {
        client.publish(
            false,
            if (checkDelivery) Qos.EXACTLY_ONCE else Qos.AT_MOST_ONCE,
            topic,
            payload,
        )
    }

    private fun handleMessage(publish: MQTTPublish) {
        logger.info { "Received RPC message from topic ${publish.topicName}" }
        val fullTopic = publish.topicName
        val match = pathRegex.find(fullTopic)
        val rpcId = match?.groupValues?.getOrNull(1)
        val remainingPath = match?.groupValues?.getOrNull(2)
        val topicName =
            remainingPath
                ?: error(
                    "Topic name does not match pattern(HomeDisplay/rpc/request/{id}/) '$fullTopic'"
                )
        if (!endpoints.containsKey(topicName)) {
            logger.warn {
                "Received a message not on a registered endpoint $fullTopic"
            }
            return
        }

        val endpoint = endpoints[topicName]!!
        val msg =
            deserializeMessage(
                publish.payload
                    ?: error("Message received on topic $fullTopic is empty"),
                endpoint.inputType,
                endpoint.isProtobuf,
            )

        val output = endpoint.function.call(msg)
        publishMessage(
            fullTopic.toResponsePath(),
            serializeMessage(
                output as Any,
                endpoint.outputType,
                endpoint.isProtobuf,
            ),
        )
    }
}
