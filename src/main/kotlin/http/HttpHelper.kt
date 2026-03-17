package net.adarw.http

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.io.InputStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import mu.KotlinLogging
import net.adarw.rpc.definitions.StatusCode

private val logger = KotlinLogging.logger {}

fun serveResourceFromJar(baseResourceDirectory: String): HttpHandler {
    return HttpHandler { exchange ->
        val requestPath =
            if (exchange.requestURI.path == "/") {
                "/index.html"
            } else {
                exchange.requestURI.path
            }

        val cleanBasePath = baseResourceDirectory.trimEnd('/')
        // The requestPath usually starts with a slash, so simple concatenation
        // works
        val resourcePath = "$cleanBasePath$requestPath"

        val inputStream: InputStream? =
            Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream(resourcePath)

        if (inputStream != null) {
            inputStream.use { stream ->
                val bytes = stream.readAllBytes()

                // 1. Determine the MIME type
                val mimeType = getMimeType(requestPath)

                // 2. Set the Content-Type header BEFORE sending response
                // headers
                exchange.responseHeaders.set("Content-Type", mimeType)

                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { os -> os.write(bytes) }
            }
        } else {
            val notFoundText = "404 Not Found"
            // It's good practice to set a plain text content type for your 404
            // message
            exchange.responseHeaders.set(
                "Content-Type",
                "text/plain; charset=UTF-8",
            )
            exchange.sendResponseHeaders(404, notFoundText.length.toLong())
            exchange.responseBody.use { os ->
                os.write(notFoundText.toByteArray())
            }
        }
    }
}

private fun getMimeType(path: String): String {
    val extension = path.substringAfterLast('.', "")

    return when (extension.lowercase()) {
        "html",
        "htm" -> "text/html; charset=UTF-8"
        "css" -> "text/css; charset=UTF-8"
        "js" -> "application/javascript; charset=UTF-8"
        "json" -> "application/json; charset=UTF-8"
        "png" -> "image/png"
        "jpg",
        "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "svg" -> "image/svg+xml"
        "ico" -> "image/x-icon"
        "woff" -> "font/woff"
        "woff2" -> "font/woff2"
        "ttf" -> "font/ttf"
        "txt" -> "text/plain; charset=UTF-8"
        "xml" -> "application/xml; charset=UTF-8"
        "pdf" -> "application/pdf"
        "zip" -> "application/zip"
        else -> "application/octet-stream"
    }
}

fun HttpExchange.sendResponse(statusCode: StatusCode, response: String) {
    sendResponseHeaders(statusCode.code, response.toByteArray().size.toLong())

    responseBody.use { os -> os.write(response.toByteArray()) }
}

fun HttpExchange.requireMethod(method: HttpMethod) {
    val valid = requestMethod == method.value
    if (!valid) {
        logger.warn {
            "Method $requestMethod is not allowed, This endpoint requires ${method.value}"
        }
        sendResponse(
            StatusCode.METHOD_NOT_ALLOWED,
            "Method $requestMethod is not allowed, This endpoint requires ${method.value}",
        )
    }
}

fun HttpExchange.getPayload(): String =
    requestBody.bufferedReader().use { it.readText() }

fun HttpExchange.getJsonPayload(): JsonElement =
    Json.parseToJsonElement(getPayload())

fun HttpHandler.registerEndpoint(path: String) =
    HttpManager.server.createContext(path, this)

fun HttpEndpoint(handler: (HttpExchange) -> Unit): HttpHandler =
    HttpHandler { exchange ->
        try {
            handler(exchange)
        } catch (e: IllegalStateException) {
            exchange.sendResponse(
                StatusCode.BAD_REQUEST,
                "Invalid state: ${e.message}",
            )
        } catch (e: IllegalArgumentException) {
            exchange.sendResponse(
                StatusCode.BAD_REQUEST,
                "Bad request: ${e.message}",
            )
        } catch (e: NoSuchElementException) {
            exchange.sendResponse(StatusCode.NOT_FOUND, "Resource not found")
        } catch (e: Exception) {
            exchange.sendResponse(
                StatusCode.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
            )
        } finally {
            exchange.close()
        }
    }
