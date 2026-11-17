package com.example.offsuitpoker.model

import java.io.Serializable

enum class RoundPhase {
    WAITING_FOR_PLAYERS,
    PRE_FLOP,
    FLOP,
    TURN,
    RIVER,
    SHOWDOWN,
    HAND_COMPLETE
}

enum class ActionType {
    FOLD,
    CHECK,
    CALL,
    BET,
    RAISE,
    ALL_IN
}

data class PlayerSeat(
    val id: String,
    val name: String,
    val stars: Int,
    var chips: Int,
    var roundBet: Int = 0,
    var totalHandBet: Int = 0,
    var cards: List<Card> = emptyList(),
    var folded: Boolean = false,
    var isAllIn: Boolean = false,
    var isSittingOut: Boolean = false,
    val isAi: Boolean = false,
    var lastActionText: String? = null
) : Serializable {
    val starsDisplay: String
        get() = if (stars > 0) "★".repeat(stars.coerceIn(0, 5)) else "OUT"
}

data class SidePot(
    val amount: Int,
    val eligiblePlayerIds: List<String>
) : Serializable

data class HandWinner(
    val playerId: String,
    val playerName: String,
    val amountWon: Int,
    val handDescription: String,
    val bestCards: List<Card>,
    val potDescription: String = ""
) : Serializable

data class RevealedHand(
    val playerName: String,
    val cards: List<Card>,
    val handDescription: String
) : Serializable

data class LastHandHistory(
    val handNumber: Int,
    val board: List<Card>,
    val pot: Int,
    val winners: List<HandWinner>,
    val hands: List<RevealedHand>
) : Serializable

data class TableState(
    val tableId: String = "local-table",
    val phase: RoundPhase = RoundPhase.WAITING_FOR_PLAYERS,
    val seats: List<PlayerSeat> = emptyList(),
    val communityCards: List<Card> = emptyList(),
    val mainPot: Int = 0,
    val sidePots: List<SidePot> = emptyList(),
    val currentHighBet: Int = 0,
    val minRaiseAmount: Int = 0,
    val smallBlind: Int = 50,
    val bigBlind: Int = 100,
    val blindLevel: Int = 1,
    val escalatingBlinds: Boolean = false,
    val dealerIndex: Int = 0,
    val smallBlindIndex: Int = -1,
    val bigBlindIndex: Int = -1,
    val activeTurnIndex: Int = -1,
    val winners: List<HandWinner> = emptyList(),
    val handCount: Int = 0,
    val hostId: String = "",
    val lastHand: LastHandHistory? = null
) : Serializable {
    val totalPot: Int
        get() = mainPot + sidePots.sumOf { it.amount } + seats.sumOf { it.roundBet }

    val activeTurnPlayer: PlayerSeat?
        get() = if (activeTurnIndex in seats.indices) seats[activeTurnIndex] else null
}

data class ActionRequest(
    val playerId: String,
    val action: ActionType,
    val amount: Int = 0
) : Serializable
