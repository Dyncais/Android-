package com.example.ihatemobile

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import okio.ByteString
import org.json.JSONObject

object WebSocketService {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    interface WebSocketCallback {
        fun onMessageReceived(message: String)
        fun onConnectionOpened()
        fun onConnectionClosed()
        fun onConnectionFailure(error: String)
    }

    fun connectWebSocket(chatID: Int, accessToken: String, callback: WebSocketCallback) {
        val request = Request.Builder()
            .url("ws://193.124.33.25:8080/api/v1/messenger/connect?chat-id=$chatID")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                callback.onConnectionOpened()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                callback.onMessageReceived(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                callback.onConnectionClosed()
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                callback.onConnectionFailure(t.message ?: "Неизвестная ошибка")
            }
        })
    }

    fun sendMessage(messageText: String) {
        webSocket?.let {
            val jsonMessage = JSONObject()
            jsonMessage.put("text", messageText)
            it.send(jsonMessage.toString())
        }
    }

    fun closeConnection() {
        webSocket?.close(1000, "Завершение соединения")
        webSocket = null
    }
}
