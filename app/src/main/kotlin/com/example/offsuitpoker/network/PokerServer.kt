package com.example.offsuitpoker.network

import com.example.offsuitpoker.engine.PokerGameEngine
import com.example.offsuitpoker.model.PlayerSeat
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class PokerServer(
    port: Int,
    private val scope: CoroutineScope,
    private val hostPlayerId: String
) : WebSocketServer(InetSocketAddress(port)) {

    private val gson = Gson()
    private val socketPlayerMap = ConcurrentHashMap<WebSocket, String>()
    private val connectedSockets = Collections.newSetFromMap(ConcurrentHashMap<WebSocket, Boolean>())

    val engine = PokerGameEngine(scope) { updatedState ->
        broadcastState(gson.toJson(updatedState))
    }

    init {
        isReuseAddr = true
        engine.setHostId(hostPlayerId)
    }

    override fun onStart() {
        println("Poker WebSocket Server started on port ${address.port}")
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake?) {
        connectedSockets.add(conn)
        // Send initial state to newly connected client
        val initialMsg = SocketMessage(
            type = "STATE",
            stateJson = gson.toJson(engine.currentState)
        )
        conn.send(gson.toJson(initialMsg))
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
        connectedSockets.remove(conn)
        val pId = socketPlayerMap.remove(conn)
        if (pId != null && pId != hostPlayerId) {
            // Player disconnected
            scope.launch(Dispatchers.Default) {
                engine.removePlayer(pId)
            }
        }
    }

    override fun onMessage(conn: WebSocket, message: String) {
        try {
            val msg = gson.fromJson(message, SocketMessage::class.java)
            when (msg.type) {
                "JOIN" -> {
                    val pId = msg.playerId ?: return
                    val pName = msg.playerName ?: "Player"
                    val stars = msg.stars ?: 5
                    val chips = msg.chips ?: 10000

                    socketPlayerMap[conn] = pId
                    val newSeat = PlayerSeat(
                        id = pId,
                        name = pName,
                        stars = stars,
                        chips = chips,
                        isAi = false
                    )
                    engine.addPlayer(newSeat)
                }

                "ACTION" -> {
                    val pId = msg.playerId ?: return
                    val action = msg.actionType ?: return
                    val amount = msg.amount ?: 0
                    engine.handleAction(pId, action, amount)
                }

                "REBUY" -> {
                    val pId = msg.playerId ?: return
                    val newStars = msg.stars ?: 0
                    val addedChips = msg.chips ?: 10000
                    engine.rebuyPlayer(pId, newStars, addedChips)
                }

                "START_HAND" -> {
                    engine.startNewHand()
                }

                "ADD_AI" -> {
                    val botPool = listOf(
                        // Indian names
                        "VIKRAM", "PRIYA", "KABIR", "AANYA", "ROHAN", "DEV", "ARJUN", "MEERA", "AARAV", "NEHA", "ADITYA", "TANYA", "ISHAN", "RIA",
                        // USA names
                        "JAKE", "ANNA", "ALEX", "SARAH", "MIKE", "EMMA", "CHRIS", "OLIVIA", "WILL", "JASON", "GRACE", "DAVID", "SAM", "EMILY", "RYAN", "LILY", "MAX", "CHLOE"
                    )
                    val existingNames = engine.currentState.seats.map { it.name.uppercase() }.toSet()
                    val available = botPool.filter { it !in existingNames }.shuffled()
                    val name = available.firstOrNull() ?: "BOT-${engine.currentState.seats.size + 1}"
                    val botSeat = PlayerSeat(
                        id = "BOT-${System.currentTimeMillis() % 10000}-${(Math.random() * 1000).toInt()}",
                        name = name.uppercase(),
                        stars = 5,
                        chips = 10000,
                        isAi = true
                    )
                    engine.addPlayer(botSeat)
                }

                "TOGGLE_BLINDS" -> {
                    engine.toggleEscalatingBlinds()
                }

                "RABBIT_HUNT" -> {
                    engine.rabbitHunt()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        ex?.printStackTrace()
    }

    private fun broadcastState(stateJsonString: String) {
        val msg = SocketMessage(
            type = "STATE",
            stateJson = stateJsonString
        )
        val jsonPayload = gson.toJson(msg)
        for (ws in connectedSockets) {
            if (ws.isOpen) {
                ws.send(jsonPayload)
            }
        }
    }
}
