package com.example.offsuitpoker.network

import com.example.offsuitpoker.model.ActionType
import com.example.offsuitpoker.model.TableState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class PokerClient(
    serverUri: URI,
    private val onConnectedCallback: (() -> Unit)? = null,
    private val onDisconnectedCallback: (() -> Unit)? = null
) : WebSocketClient(serverUri) {

    private val gson = Gson()
    private val _tableState = MutableStateFlow(TableState())
    val tableState: StateFlow<TableState> = _tableState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    override fun onOpen(handshakedata: ServerHandshake?) {
        _isConnected.value = true
        onConnectedCallback?.invoke()
    }

    override fun onMessage(message: String) {
        try {
            val msg = gson.fromJson(message, SocketMessage::class.java)
            if (msg.type == "STATE" && msg.stateJson != null) {
                val state = gson.fromJson(msg.stateJson, TableState::class.java)
                _tableState.value = state
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        _isConnected.value = false
        onDisconnectedCallback?.invoke()
    }

    override fun onError(ex: Exception?) {
        ex?.printStackTrace()
    }

    fun joinTable(playerId: String, playerName: String, stars: Int, chips: Int) {
        val msg = SocketMessage(
            type = "JOIN",
            playerId = playerId,
            playerName = playerName,
            stars = stars,
            chips = chips
        )
        send(gson.toJson(msg))
    }

    fun sendAction(playerId: String, action: ActionType, amount: Int = 0) {
        val msg = SocketMessage(
            type = "ACTION",
            playerId = playerId,
            actionType = action,
            amount = amount
        )
        send(gson.toJson(msg))
    }

    fun sendRebuy(playerId: String, newStars: Int, addedChips: Int = 10000) {
        val msg = SocketMessage(
            type = "REBUY",
            playerId = playerId,
            stars = newStars,
            chips = addedChips
        )
        send(gson.toJson(msg))
    }

    fun startHand() {
        val msg = SocketMessage(type = "START_HAND")
        send(gson.toJson(msg))
    }

    fun addAiBot() {
        val msg = SocketMessage(type = "ADD_AI")
        send(gson.toJson(msg))
    }

    fun toggleBlinds() {
        val msg = SocketMessage(type = "TOGGLE_BLINDS")
        send(gson.toJson(msg))
    }

    fun rabbitHunt() {
        val msg = SocketMessage(type = "RABBIT_HUNT")
        send(gson.toJson(msg))
    }
}
