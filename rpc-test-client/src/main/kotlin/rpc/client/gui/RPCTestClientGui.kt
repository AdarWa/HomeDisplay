@file:OptIn(ExperimentalUnsignedTypes::class)

package net.adarw.rpc.client.gui

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64
import java.util.Collections
import javax.swing.BorderFactory
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTabbedPane
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.border.EmptyBorder
import net.adarw.rpc.client.RPCTestClient
import net.adarw.rpc.client.TestClientConfig

private const val DEFAULT_SCHEMA_DIR = "src/main/proto"

object RPCTestClientGui {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater { RPCTestClientGuiWindow().isVisible = true }
    }
}

private class RPCTestClientGuiWindow : JFrame("HomeDisplay RPC Test Client") {
    private var client: RPCTestClient? = null
    private val base64 = Base64.getDecoder()
    private val base64Encoder = Base64.getEncoder()

    private val brokerField = JTextField(18)
    private val portField = JTextField(6)
    private val clientIdField = JTextField(14)
    private val baseTopicField = JTextField(16)
    private val usernameField = JTextField(10)
    private val passwordField = JTextField(10)
    private val endpointField = JTextField(18)
    private val timeoutField = JTextField(6)

    private val jsonRequestArea = JTextArea(10, 50)
    private val jsonResponseArea = JTextArea(10, 50)
    private val protoRequestArea = JTextArea(10, 50)
    private val protoResponseArea = JTextArea(10, 50)
    private val logArea = JTextArea(6, 80)

    private val schemaListModel = DefaultListModel<Path>()
    private val schemaList = JList(schemaListModel)
    private val schemaContentArea = JTextArea(12, 40)
    private val schemaDirField = JTextField(DEFAULT_SCHEMA_DIR, 18)
    private val schemaRefreshButton = JButton("Reload Schemas")
    private val schemaSelector = JComboBox<String>()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(1100, 820)
        val rootPanel = JPanel(BorderLayout(8, 8))
        rootPanel.border = EmptyBorder(12, 12, 12, 12)
        contentPane = rootPanel

        add(buildConnectionPanel(), BorderLayout.NORTH)
        add(buildMainPanel(), BorderLayout.CENTER)
        add(buildLogPanel(), BorderLayout.SOUTH)

        jsonResponseArea.isEditable = false
        protoResponseArea.isEditable = false
        schemaContentArea.isEditable = false
        logArea.isEditable = false

        loadSchemas()
        addSchemaListeners()
        updateSchemaSelector()
    }

    private fun buildConnectionPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.border = BorderFactory.createTitledBorder("Connection")
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(4, 4, 4, 4)
        }

        val config = TestClientConfig()
        brokerField.text = config.broker
        portField.text = config.port.toString()
        clientIdField.text = config.clientId
        baseTopicField.text = config.baseTopic
        usernameField.text = config.username ?: ""
        passwordField.text = config.password?.toByteArray()?.decodeToString() ?: ""
        timeoutField.text = config.timeoutMs.toString()

        var col = 0
        addField(panel, gbc, col, 0, "Broker", brokerField)
        col++
        addField(panel, gbc, col, 0, "Port", portField)
        col++
        addField(panel, gbc, col, 0, "Client ID", clientIdField)
        col++
        addField(panel, gbc, col, 0, "Base Topic", baseTopicField)

        col = 0
        addField(panel, gbc, col, 1, "Username", usernameField)
        col++
        addField(panel, gbc, col, 1, "Password", passwordField)
        col++
        addField(panel, gbc, col, 1, "Timeout (ms)", timeoutField)

        val connectButton = JButton("Connect")
        val disconnectButton = JButton("Disconnect")
        connectButton.addActionListener { connectClient() }
        disconnectButton.addActionListener { disconnectClient() }

        gbc.gridx = 6
        gbc.gridy = 0
        gbc.gridheight = 2
        gbc.fill = GridBagConstraints.BOTH
        panel.add(buildButtonPanel(connectButton, disconnectButton), gbc)

        return panel
    }

    private fun buildMainPanel(): JPanel {
        val mainPanel = JPanel(BorderLayout(8, 8))

        val endpointPanel = JPanel(GridBagLayout())
        endpointPanel.border = BorderFactory.createTitledBorder("Request")
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(4, 4, 4, 4)
        }
        addField(endpointPanel, gbc, 0, 0, "Endpoint", endpointField, 5)
        mainPanel.add(endpointPanel, BorderLayout.NORTH)

        val tabs = JTabbedPane()
        tabs.addTab("JSON", buildJsonPanel())
        tabs.addTab("Protobuf", buildProtoPanel())
        mainPanel.add(tabs, BorderLayout.CENTER)

        mainPanel.add(buildSchemaPanel(), BorderLayout.EAST)
        return mainPanel
    }

    private fun buildJsonPanel(): JPanel {
        val panel = JPanel(BorderLayout(6, 6))
        val request = buildEditorPanel("JSON Request", jsonRequestArea)
        val response = buildEditorPanel("JSON Response", jsonResponseArea)
        val sendButton = JButton("Send JSON")
        sendButton.addActionListener { sendJson() }
        panel.add(buildSplitPanel(request, response), BorderLayout.CENTER)
        panel.add(sendButton, BorderLayout.SOUTH)
        return panel
    }

    private fun buildProtoPanel(): JPanel {
        val panel = JPanel(BorderLayout(6, 6))
        val request =
            buildEditorPanel(
                "Protobuf Request (Base64)",
                protoRequestArea,
            )
        val response =
            buildEditorPanel(
                "Protobuf Response (Base64)",
                protoResponseArea,
            )
        val sendButton = JButton("Send Protobuf")
        sendButton.addActionListener { sendProto() }
        panel.add(buildSplitPanel(request, response), BorderLayout.CENTER)
        panel.add(sendButton, BorderLayout.SOUTH)
        return panel
    }

    private fun buildSchemaPanel(): JPanel {
        val panel = JPanel(BorderLayout(6, 6))
        panel.border = BorderFactory.createTitledBorder("Schemas")

        val schemaControls = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(4, 4, 4, 4)
        }
        addField(schemaControls, gbc, 0, 0, "Schema Dir", schemaDirField, 2)
        gbc.gridx = 2
        gbc.gridy = 0
        schemaControls.add(schemaRefreshButton, gbc)

        val listScroll = JScrollPane(schemaList)
        listScroll.preferredSize = Dimension(220, 180)
        val contentScroll = JScrollPane(schemaContentArea)

        val selectorPanel = JPanel(BorderLayout(4, 4))
        selectorPanel.add(JLabel("Quick Select"), BorderLayout.NORTH)
        selectorPanel.add(schemaSelector, BorderLayout.CENTER)

        panel.add(schemaControls, BorderLayout.NORTH)
        panel.add(listScroll, BorderLayout.CENTER)
        panel.add(selectorPanel, BorderLayout.SOUTH)
        panel.add(contentScroll, BorderLayout.EAST)
        return panel
    }

    private fun buildLogPanel(): JPanel {
        val panel = JPanel(BorderLayout(4, 4))
        panel.border = BorderFactory.createTitledBorder("Log")
        panel.add(JScrollPane(logArea), BorderLayout.CENTER)
        return panel
    }

    private fun buildEditorPanel(title: String, area: JTextArea): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(title)
        panel.add(JScrollPane(area), BorderLayout.CENTER)
        return panel
    }

    private fun buildSplitPanel(left: JPanel, right: JPanel): JPanel {
        val panel = JPanel(BorderLayout(6, 6))
        panel.add(left, BorderLayout.WEST)
        panel.add(right, BorderLayout.CENTER)
        return panel
    }

    private fun buildButtonPanel(vararg buttons: JButton): JPanel {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply { insets = Insets(4, 4, 4, 4) }
        buttons.forEachIndexed { index, button ->
            gbc.gridx = index
            panel.add(button, gbc)
        }
        return panel
    }

    private fun addField(
        panel: JPanel,
        gbc: GridBagConstraints,
        x: Int,
        y: Int,
        label: String,
        field: JTextField,
        span: Int = 1,
    ) {
        gbc.gridx = x * 2
        gbc.gridy = y
        gbc.gridwidth = 1
        panel.add(JLabel(label), gbc)

        gbc.gridx = x * 2 + 1
        gbc.gridy = y
        gbc.gridwidth = span
        panel.add(field, gbc)
    }

    private fun connectClient() {
        disconnectClient()
        try {
            val config =
                TestClientConfig(
                    broker = brokerField.text.trim(),
                    port = portField.text.trim().toInt(),
                    clientId = clientIdField.text.trim(),
                    baseTopic = baseTopicField.text.trim(),
                    timeoutMs = timeoutField.text.trim().toLong(),
                    username = usernameField.text.trim().ifEmpty { null },
                    password =
                        passwordField.text.trim().ifEmpty { null }?.encodeToByteArray()
                            ?.toUByteArray(),
                )
            client = RPCTestClient(config)
            log("Connected to ${config.broker}:${config.port}")
        } catch (ex: Exception) {
            showError("Failed to connect: ${ex.message}")
        }
    }

    private fun disconnectClient() {
        client?.close()
        client = null
        log("Disconnected")
    }

    private fun sendJson() {
        val endpoint = endpointField.text.trim()
        if (endpoint.isEmpty()) {
            showError("Endpoint is required")
            return
        }
        val payload = jsonRequestArea.text
        runRequest("JSON", payload) { client ->
            val response = client.requestJson(endpoint, payload)
            jsonResponseArea.text = response
        }
    }

    private fun sendProto() {
        val endpoint = endpointField.text.trim()
        if (endpoint.isEmpty()) {
            showError("Endpoint is required")
            return
        }
        val base64Payload = protoRequestArea.text.trim()
        if (base64Payload.isEmpty()) {
            showError("Base64 payload required")
            return
        }
        runRequest("Protobuf", base64Payload) { client ->
            val bytes = base64.decode(base64Payload)
            val response =
                client.requestRaw(endpoint, bytes.toUByteArray())
            protoResponseArea.text =
                base64Encoder.encodeToString(response.toByteArray())
        }
    }

    private fun runRequest(
        label: String,
        payloadPreview: String,
        request: (RPCTestClient) -> Unit,
    ) {
        val activeClient = client
        if (activeClient == null) {
            showError("Client not connected")
            return
        }
        val worker =
            object : SwingWorker<Unit, Unit>() {
                override fun doInBackground() {
                    log("Sending $label request")
                    request(activeClient)
                    log("Received $label response")
                }

                override fun done() {
                    try {
                        get()
                    } catch (ex: Exception) {
                        showError("$label request failed: ${ex.cause?.message ?: ex.message}")
                    }
                }
            }
        worker.execute()
        log("Payload preview: ${payloadPreview.take(120)}")
    }

    private fun loadSchemas() {
        schemaListModel.removeAllElements()
        schemaSelector.removeAllItems()
        val schemaDir = Path.of(schemaDirField.text.trim())
        if (!Files.exists(schemaDir)) {
            schemaContentArea.text = "Schema directory not found: $schemaDir"
            return
        }
        Files.walk(schemaDir).use { paths ->
            paths
                .filter { Files.isRegularFile(it) && it.toString().endsWith(".proto") }
                .sorted()
                .forEach { path ->
                    schemaListModel.addElement(path)
                    schemaSelector.addItem(path.fileName.toString())
                }
        }
        if (schemaListModel.size() > 0) {
            schemaList.selectedIndex = 0
            showSchema(schemaListModel.firstElement())
        }
    }

    private fun addSchemaListeners() {
        schemaList.addListSelectionListener {
            val selected = schemaList.selectedValue ?: return@addListSelectionListener
            showSchema(selected)
        }
        schemaSelector.addActionListener {
            val selected = schemaSelector.selectedItem as? String ?: return@addActionListener
            val match =
                Collections.list(schemaListModel.elements()).firstOrNull {
                    it.fileName.toString() == selected
                }
            if (match != null) {
                schemaList.setSelectedValue(match, true)
            }
        }
        schemaRefreshButton.addActionListener { loadSchemas() }
    }

    private fun updateSchemaSelector() {
        schemaSelector.maximumRowCount = 12
    }

    private fun showSchema(path: Path) {
        schemaContentArea.text = Files.readString(path)
    }

    private fun log(message: String) {
        logArea.append("$message\n")
    }

    private fun showError(message: String) {
        log("Error: $message")
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE)
    }
}
