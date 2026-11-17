package com.example.offsuitpoker.network

import com.example.offsuitpoker.util.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

class PokerHttpServer(
    private val port: Int = 8080,
    private val scope: CoroutineScope
) {
    private var serverSocket: ServerSocket? = null
    private var listenerJob: Job? = null

    fun start() {
        if (listenerJob?.isActive == true) return
        listenerJob = scope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(port).apply {
                    reuseAddress = true
                }
                AppLogger.i("PokerHttpServer", "Web Client HTTP Server started on port $port")
                while (isActive) {
                    val clientSocket = try {
                        serverSocket?.accept()
                    } catch (_: Exception) {
                        null
                    } ?: break

                    launch(Dispatchers.IO) {
                        handleClient(clientSocket)
                    }
                }
            } catch (e: Exception) {
                AppLogger.e("PokerHttpServer", "HTTP Server encountered error on port $port: ${e.message}")
            }
        }
    }

    private fun handleClient(socket: Socket) {
        socket.use { s ->
            try {
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                val requestLine = reader.readLine() ?: return
                // Check if request is GET
                if (requestLine.startsWith("GET")) {
                    val html = WebClientHtml.getHtml()
                    val htmlBytes = html.toByteArray(Charsets.UTF_8)

                    val responseHeaders = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=UTF-8\r\n" +
                            "Content-Length: ${htmlBytes.size}\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Connection: close\r\n\r\n"

                    val out = s.getOutputStream()
                    out.write(responseHeaders.toByteArray(Charsets.UTF_8))
                    out.write(htmlBytes)
                    out.flush()
                }
            } catch (_: Exception) {
                // Client closed connection
            }
        }
    }

    fun stop() {
        try {
            listenerJob?.cancel()
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        listenerJob = null
        AppLogger.i("PokerHttpServer", "Web Client HTTP Server stopped")
    }
}
