package net.adarw.http

import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import mu.KotlinLogging
import net.adarw.config.Environment
import java.io.InputStream
import java.net.InetSocketAddress

object HttpManager {

    private val server = HttpServer.create(InetSocketAddress("0.0.0.0",Environment.HTTP_PORT), 0)
    private val logger = KotlinLogging.logger {  }
    private val thread = Thread {
        startServer()
    }.apply {
        name = "http"
    }

    init {
        thread.start()
    }

    private fun serveResourceFromJar(baseResourceDirectory: String): HttpHandler {
        return HttpHandler { exchange ->
            val requestPath = if (exchange.requestURI.path == "/") {
                "/index.html"
            } else {
                exchange.requestURI.path
            }

            val cleanBasePath = baseResourceDirectory.trimEnd('/')
            val resourcePath = "$cleanBasePath$requestPath"

            val inputStream: InputStream? = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)

            if (inputStream != null) {
                inputStream.use { stream ->
                    val bytes = stream.readAllBytes()

                    exchange.sendResponseHeaders(200, bytes.size.toLong())
                    exchange.responseBody.use { os ->
                        os.write(bytes)
                    }
                }
            } else {
                val notFoundText = "404 Not Found"
                exchange.sendResponseHeaders(404, notFoundText.length.toLong())
                exchange.responseBody.use { os ->
                    os.write(notFoundText.toByteArray())
                }
            }
        }
    }

    private fun startServer(){
        logger.info { "Starting HTTP server on http://0.0.0.0:${Environment.HTTP_PORT}/" }

        server.createContext("/", serveResourceFromJar("static"))

        server.executor = null
        server.start()
    }
}