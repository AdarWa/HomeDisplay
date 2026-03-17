@file:OptIn(ExperimentalUnsignedTypes::class)

package net.adarw.rpc.client.gui

import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.MQTTVersion
import io.github.davidepianca98.mqtt.Subscription
import io.github.davidepianca98.mqtt.packets.Qos
import io.github.davidepianca98.mqtt.packets.mqtt.MQTTPublish
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SwingUtilities
import net.adarw.config.Environment
import net.adarw.rpc.toResponsePath

class RPCTestClientGui : JFrame("RPC Test Client") {
    private val endpointField = JTextField("", 40)
    private val payloadArea = JTextArea(10, 60)
    private val responseArea = JTextArea(10, 60)
    private val statusLabel = JLabel("Disconnected")
    private val requestCounter = AtomicInteger(1)
    private val responseHandlers = ConcurrentHashMap<String, (MQTTPublish) -> Unit>()

    private val client =
        MQTTClient(
            MQTTVersion.MQTT5,
            Environment.MQTT_BROKER,
            Environment.MQTT_PORT,
            clientId = "rpc-test-client",
            userName = Environment.MQTT_AUTH.user,
            password = Environment.MQTT_AUTH.password?.encodeToByteArray()?.toUByteArray(),
            tls = null,
        ) { publish ->
            responseHandlers[publish.topicName]?.invoke(publish)
        }

    private val clientThread =
        Thread {
                client.run()
            }
            .apply { name = "rpc-test-client" }

    init {
        responseArea.isEditable = false
        payloadArea.lineWrap = true
        responseArea.lineWrap = true

        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout(8, 8)
        add(buildFormPanel(), BorderLayout.CENTER)
        add(buildResponsePanel(), BorderLayout.SOUTH)

        size = Dimension(800, 640)
        setLocationRelativeTo(null)

        clientThread.start()
        statusLabel.text = "Connected to ${Environment.MQTT_BROKER}:${Environment.MQTT_PORT}"
    }

    private fun buildFormPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            weightx = 0.0
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(4, 4, 4, 4)
        }

        panel.add(JLabel("Endpoint"), constraints)

        constraints.gridx = 1
        constraints.weightx = 1.0
        panel.add(endpointField, constraints)

        constraints.gridx = 0
        constraints.gridy = 1
        constraints.weightx = 0.0
        panel.add(JLabel("Payload"), constraints)

        constraints.gridx = 1
        constraints.weightx = 1.0
        constraints.weighty = 1.0
        constraints.fill = GridBagConstraints.BOTH
        panel.add(JScrollPane(payloadArea), constraints)

        constraints.gridx = 1
        constraints.gridy = 2
        constraints.weighty = 0.0
        constraints.fill = GridBagConstraints.NONE
        constraints.anchor = GridBagConstraints.EAST
        val sendButton = JButton("Send")
        sendButton.addActionListener { sendRequest() }
        panel.add(sendButton, constraints)

        return panel
    }

    private fun buildResponsePanel(): JPanel {
        val panel = JPanel(BorderLayout(4, 4))
        panel.border = BorderFactory.createTitledBorder("Response")
        panel.add(JScrollPane(responseArea), BorderLayout.CENTER)
        panel.add(statusLabel, BorderLayout.SOUTH)
        return panel
    }

    private fun sendRequest() {
        val endpoint = endpointField.text.trim()
        if (endpoint.isBlank()) {
            responseArea.text = "Endpoint is required."
            return
        }

        val requestId = requestCounter.getAndIncrement()
        val requestTopic = "HomeDisplay/rpc/request/$requestId/$endpoint"
        val responseTopic = requestTopic.toResponsePath()
        val payloadBytes = payloadArea.text.encodeToByteArray().toUByteArray()

        responseArea.text = "Waiting for response on $responseTopic..."
        statusLabel.text = "Sending $requestTopic"

        responseHandlers[responseTopic] = { publish ->
            responseHandlers.remove(responseTopic)
            val responseText = formatPayload(publish.payload)
            SwingUtilities.invokeLater {
                responseArea.text = responseText
                statusLabel.text = "Received response from ${publish.topicName}"
            }
        }

        client.subscribe(listOf(Subscription(responseTopic)))
        client.publish(false, Qos.AT_MOST_ONCE, requestTopic, payloadBytes)
    }

    private fun formatPayload(payload: UByteArray?): String {
        if (payload == null || payload.isEmpty()) {
            return "<empty response>"
        }

        val bytes = payload.toByteArray()
        val decoded = runCatching { bytes.decodeToString() }.getOrNull()
        return if (decoded != null && decoded.none { it == '\uFFFD' }) {
            decoded
        } else {
            "<binary response>\n${Base64.getEncoder().encodeToString(bytes)}"
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater { RPCTestClientGui().isVisible = true }
        }
    }
}
