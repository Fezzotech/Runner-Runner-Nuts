package com.example.offsuitpoker.network

import com.example.offsuitpoker.model.ActionType
import com.example.offsuitpoker.model.TableState
import java.io.Serializable

data class SocketMessage(
    val type: String, // "JOIN", "ACTION", "REBUY", "START_HAND", "ADD_AI", "STATE", "ERROR"
    val playerId: String? = null,
    val playerName: String? = null,
    val stars: Int? = null,
    val chips: Int? = null,
    val actionType: ActionType? = null,
    val amount: Int? = null,
    val stateJson: String? = null,
    val error: String? = null
) : Serializable
