package net.adarw.http

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import mu.KotlinLogging
import net.adarw.config.Environment

object HttpManager {

    val server: HttpServer =
        HttpServer.create(
            InetSocketAddress("0.0.0.0", Environment.HTTP_PORT),
            0,
        )
    private val logger = KotlinLogging.logger {}
    private val thread = Thread { startServer() }.apply { name = "http" }

    init {
        thread.start()
    }

    private fun startServer() {
        logger.info {
            "Starting HTTP server on http://0.0.0.0:${Environment.HTTP_PORT}/"
        }

        loadDefinitions()

        server.createContext("/", serveResourceFromJar("static"))

        server.executor = null
        server.start()
    }
}
