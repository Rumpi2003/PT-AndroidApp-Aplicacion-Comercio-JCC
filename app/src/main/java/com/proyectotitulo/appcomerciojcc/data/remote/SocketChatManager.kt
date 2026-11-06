package com.proyectotitulo.appcomerciojcc.data.remote

import android.os.Handler
import android.os.Looper
import com.proyectotitulo.appcomerciojcc.domain.models.GetMessageHistoryResponse
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONObject

class SocketChatManager {

    private var socket: Socket? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var globalTransactionStatusListener: ((Int, String) -> Unit)? = null
    private var globalNewMessageListener: ((GetMessageHistoryResponse.Message) -> Unit)? = null
    private var globalMessagesReadListener: ((Int) -> Unit)? = null
    private var globalChatClosedListener: (() -> Unit)? = null

    private fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post { action() }
        }
    }

    fun setListeners(
        onNewMessage: (GetMessageHistoryResponse.Message) -> Unit = {},
        onMessagesRead: (Int) -> Unit = {},
        onChatClosed: () -> Unit = {},
        onTransactionStatusChanged: (Int, String) -> Unit = { _, _ -> }
    ) {
        globalNewMessageListener = onNewMessage
        globalMessagesReadListener = onMessagesRead
        globalChatClosedListener = onChatClosed
        globalTransactionStatusListener = onTransactionStatusChanged
    }

    fun ensureGlobalConnection(
        onTransactionStatusChanged: (Int, String) -> Unit = { _, _ -> }
    ) {
        globalTransactionStatusListener = onTransactionStatusChanged

        if (socket != null && socket?.connected() == true) {
            return
        }

        try {
            disconnect()

            val baseUrl = KtorApiClient.BASE_URL.replace("/api", "")
            val token = KtorApiClient.token.orEmpty()

            if (token.isBlank()) return

            val opts = IO.Options().apply {
                forceNew = true
                reconnection = true
                transports = arrayOf(WebSocket.NAME)
                auth = mapOf("token" to token)
            }

            val newSocket = IO.socket(baseUrl, opts)
            socket = newSocket

            newSocket.on(Socket.EVENT_CONNECT) {
                // Conectado exitosamente al socket global
            }

            newSocket.on("chat:mensaje") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as? JSONObject ?: return@on
                    val msgId = data.optInt("id_mensaje")
                    val txId = data.optInt("id_transaccion")
                    val senderId = data.optInt("id_emisor")
                    val content = data.optString("contenido")
                    val isRead = data.optBoolean("leido")
                    val dateSent = data.optString("fecha_envio")

                    val message = GetMessageHistoryResponse.Message(
                        messageId = msgId,
                        transactionId = txId,
                        senderId = senderId,
                        content = content,
                        isRead = isRead,
                        dateSent = dateSent
                    )
                    runOnMainThread { globalNewMessageListener?.invoke(message) }
                }
            }

            newSocket.on("chat:leido") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as? JSONObject ?: return@on
                    val hastaMsgId = data.optInt("hasta_id_mensaje")
                    runOnMainThread { globalMessagesReadListener?.invoke(hastaMsgId) }
                }
            }

            newSocket.on("chat:cerrado") {
                runOnMainThread { globalChatClosedListener?.invoke() }
            }

            newSocket.on("transaccion:estado") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as? JSONObject ?: return@on
                    val txId = data.optInt("id_transaccion")
                    val estado = data.optString("estado")
                    runOnMainThread { globalTransactionStatusListener?.invoke(txId, estado) }
                }
            }

            newSocket.connect()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun joinChatRoom(transactionId: Int, onConnected: () -> Unit = {}) {
        val s = socket ?: run {
            ensureGlobalConnection()
            socket
        }

        val payload = JSONObject().apply {
            put("id_transaccion", transactionId)
        }

        s?.emit("chat:unirse", payload, Ack { args ->
            if (args.isNotEmpty()) {
                val response = args[0] as? JSONObject
                val ok = response?.optBoolean("ok") ?: false
                if (ok) {
                    runOnMainThread { onConnected() }
                }
            }
        })
    }

    fun sendMessage(
        transactionId: Int,
        content: String,
        onSuccess: (GetMessageHistoryResponse.Message?) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentSocket = socket ?: run {
            runOnMainThread { onError("Socket no conectado") }
            return
        }

        val payload = JSONObject().apply {
            put("id_transaccion", transactionId)
            put("contenido", content)
        }

        currentSocket.emit("chat:enviar", payload, Ack { args ->
            if (args.isNotEmpty()) {
                val response = args[0] as? JSONObject
                val ok = response?.optBoolean("ok") ?: false
                if (ok) {
                    val dataObj = response?.optJSONObject("data")
                    val msgId = dataObj?.optInt("id_mensaje")
                    val senderId = dataObj?.optInt("id_emisor")
                    val isRead = dataObj?.optBoolean("leido") ?: false
                    val dateSent = dataObj?.optString("fecha_envio")

                    val sentMessage = GetMessageHistoryResponse.Message(
                        messageId = msgId,
                        transactionId = transactionId,
                        senderId = senderId,
                        content = content,
                        isRead = isRead,
                        dateSent = dateSent
                    )
                    runOnMainThread { onSuccess(sentMessage) }
                } else {
                    val errMsg = response?.optString("message") ?: "Error al enviar mensaje"
                    runOnMainThread { onError(errMsg) }
                }
            } else {
                runOnMainThread { onError("Sin respuesta del servidor") }
            }
        })
    }

    fun markAsRead(transactionId: Int, hastaMessageId: Int) {
        val currentSocket = socket ?: return
        val payload = JSONObject().apply {
            put("id_transaccion", transactionId)
            put("hasta_id_mensaje", hastaMessageId)
        }
        currentSocket.emit("chat:leido", payload, Ack {})
    }

    fun leaveRoom(transactionId: Int) {
        socket?.let { s ->
            if (s.connected()) {
                val payload = JSONObject().apply {
                    put("id_transaccion", transactionId)
                }
                s.emit("chat:salir", payload, Ack {})
            }
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }
}
