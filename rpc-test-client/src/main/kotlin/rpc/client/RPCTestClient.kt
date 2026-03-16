@file:OptIn(
    ExperimentalSerializationApi::class,
    ExperimentalUnsignedTypes::class,
)

package net.adarw.rpc.client

import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.MQTTVersion
import io.github.davidepianca98.mqtt.Subscription
import io.github.davidepianca98.mqtt.packets.Qos
import io.github.davidepianca98.mqtt.packets.mqtt.MQTTPublish
import io.github.davidepianca98.mqtt.packets.mqttv5.ReasonCode
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass
import kotlinx.serialization.ExperimentalSerializationApi
import net.adarw.config.Environment
import net.adarw.rpc.deserializeMessage
import net.adarw.rpc.serializeMessage

data class TestClientConfig(
    val broker: String = Environment.MQTT_BROKER,
    val port: Int = Environment.MQTT_PORT,
    val clientId: String = "${Environment.MQTT_CLIENT_ID}-test",
    val baseTopic: String = "HomeDisplay/rpc",
    val timeoutMs: Long = 5_000,
    val username: String? = Environment.MQTT_AUTH.user,
    val password: UByteArray? =
        Environment.MQTT_AUTH.password?.encodeToByteArray()?.toUByteArray(),
)

class RPCTestClient(val config: TestClientConfig = TestClientConfig()) :
    AutoCloseable {
    private data class PendingRequest(
        val requestPayload: UByteArray,
        val future: CompletableFuture<UByteArray>,
        val sawEcho: AtomicBoolean = AtomicBoolean(false),
    )

    private val pendingRequests = ConcurrentHashMap<String, PendingRequest>()
    private val listeners =
        CopyOnWriteArrayList<Pair<String, (String, UByteArray) -> Unit>>()
    private val requestCounter = AtomicLong(1)

    private val client =
        MQTTClient(
            MQTTVersion.MQTT5,
            config.broker,
            config.port,
            clientId = config.clientId,
            userName = config.username,
            password = config.password,
            tls = null,
        ) { publish ->
            handlePublish(publish)
        }

    init {
        client.subscribe(listOf(Subscription("${config.baseTopic}/#")))
    }

    fun listen(topicFilter: String, handler: (String, UByteArray) -> Unit) {
        listeners += topicFilter to handler
        client.subscribe(listOf(Subscription(topicFilter)))
    }

    fun publishRaw(
        endpoint: String,
        payload: UByteArray,
        requestId: String = nextRequestId(),
    ) {
        client.publish(false, Qos.AT_MOST_ONCE, topicFor(endpoint, requestId), payload)
    }

    fun requestRaw(
        endpoint: String,
        payload: UByteArray,
        requestId: String = nextRequestId(),
        timeoutMs: Long = config.timeoutMs,
    ): UByteArray {
        val topic = topicFor(endpoint, requestId)
        val future = CompletableFuture<UByteArray>()
        pendingRequests[topic] = PendingRequest(payload, future)
        client.publish(false, Qos.AT_MOST_ONCE, topic, payload)
        return try {
            future.get(timeoutMs, TimeUnit.MILLISECONDS)
        } finally {
            pendingRequests.remove(topic)
        }
    }

    fun requestJson(endpoint: String, requestJson: String): String {
        val response =
            requestRaw(endpoint, requestJson.encodeToByteArray().toUByteArray())
        return response.toByteArray().decodeToString()
    }

    fun publishJson(endpoint: String, requestJson: String) {
        publishRaw(endpoint, requestJson.encodeToByteArray().toUByteArray())
    }

    fun <T : Any, R : Any> request(
        endpoint: String,
        request: T,
        requestType: KClass<T>,
        responseType: KClass<R>,
        isProtobuf: Boolean = true,
        requestId: String = nextRequestId(),
        timeoutMs: Long = config.timeoutMs,
    ): R {
        val payload = serializeMessage(request, requestType, isProtobuf)
        val response = requestRaw(endpoint, payload, requestId, timeoutMs)
        return deserializeMessage(response, responseType, isProtobuf)
    }

    inline fun <reified T : Any, reified R : Any> request(
        endpoint: String,
        request: T,
        isProtobuf: Boolean = true,
        requestId: String = nextRequestId(),
        timeoutMs: Long = config.timeoutMs,
    ): R =
        request(
            endpoint,
            request,
            T::class,
            R::class,
            isProtobuf,
            requestId,
            timeoutMs,
        )

    inline fun <reified T : Any, reified R : Any> requestJson(
        endpoint: String,
        request: T,
        requestId: String = nextRequestId(),
        timeoutMs: Long = config.timeoutMs,
    ): R = request(endpoint, request, isProtobuf = false, requestId, timeoutMs)

    inline fun <reified T : Any, reified R : Any> requestProto(
        endpoint: String,
        request: T,
        requestId: String = nextRequestId(),
        timeoutMs: Long = config.timeoutMs,
    ): R = request(endpoint, request, isProtobuf = true, requestId, timeoutMs)

    fun <R : MessageLite> requestProtobuf(
        endpoint: String,
        request: MessageLite,
        responseParser: Parser<R>,
        requestId: String = nextRequestId(),
        timeoutMs: Long = config.timeoutMs,
    ): R {
        val response =
            requestRaw(
                endpoint,
                request.toByteArray().toUByteArray(),
                requestId,
                timeoutMs,
            )
        return responseParser.parseFrom(response.toByteArray())
    }

    fun publishProtobuf(endpoint: String, request: MessageLite) {
        publishRaw(endpoint, request.toByteArray().toUByteArray())
    }

    override fun close() {
        client.disconnect(ReasonCode.DISCONNECT_WITH_WILL_MESSAGE)
    }

    private fun handlePublish(publish: MQTTPublish) {
        val payload = publish.payload ?: return
        val topic = publish.topicName
        val pending = pendingRequests[topic]
        if (pending != null) {
            if (
                payload.contentEquals(pending.requestPayload) &&
                    !pending.sawEcho.getAndSet(true)
            ) {
                return
            }
            pending.future.complete(payload)
            return
        }
        listeners.forEach { (filter, handler) ->
            if (topicMatches(filter, topic)) {
                handler(topic, payload)
            }
        }
    }

    private fun topicFor(endpoint: String, requestId: String): String {
        val normalizedEndpoint = endpoint.trimStart('/')
        return "${config.baseTopic}/$requestId/$normalizedEndpoint"
    }

    fun nextRequestId(): String = requestCounter.getAndIncrement().toString()

    private fun topicMatches(filter: String, topic: String): Boolean {
        val filterParts = filter.split("/")
        val topicParts = topic.split("/")
        var index = 0
        while (index < filterParts.size) {
            val filterPart = filterParts[index]
            if (filterPart == "#") {
                return true
            }
            if (index >= topicParts.size) {
                return false
            }
            if (filterPart != "+" && filterPart != topicParts[index]) {
                return false
            }
            index++
        }
        return index == topicParts.size
    }
}
