package com.example.offsuitpoker.engine

import com.example.offsuitpoker.model.ActionRequest
import com.example.offsuitpoker.model.ActionType
import com.example.offsuitpoker.model.Card
import com.example.offsuitpoker.model.Deck
import com.example.offsuitpoker.model.HandWinner
import com.example.offsuitpoker.model.LastHandHistory
import com.example.offsuitpoker.model.PlayerSeat
import com.example.offsuitpoker.model.RevealedHand
import com.example.offsuitpoker.model.RoundPhase
import com.example.offsuitpoker.model.SidePot
import com.example.offsuitpoker.model.TableState
import com.example.offsuitpoker.util.AppLogger
import com.example.offsuitpoker.util.CurrencyFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class PokerGameEngine(
    private val scope: CoroutineScope,
    private val onStateUpdated: (TableState) -> Unit
) {
    private val deck = Deck()
    private val seats = mutableListOf<PlayerSeat>()
    private val communityCards = mutableListOf<Card>()
    private var state = TableState()

    // Betting round bookkeeping
    private var playersActedInRound = mutableSetOf<String>()
    private var lastRaiserIndex = -1

    init {
        pushState()
    }

    val currentState: TableState
        get() = state

    @Synchronized
    fun setHostId(hostId: String) {
        state = state.copy(hostId = hostId)
        pushState()
    }

    @Synchronized
    fun addPlayer(player: PlayerSeat): Boolean {
        if (seats.size >= 8 || seats.any { it.id == player.id }) return false
        seats.add(player)
        pushState()
        return true
    }

    @Synchronized
    fun removePlayer(playerId: String) {
        seats.removeAll { it.id == playerId }
        if (state.activeTurnPlayer?.id == playerId) {
            advanceTurn()
        }
        pushState()
    }

    @Synchronized
    fun rebuyPlayer(playerId: String, newStars: Int, addedChips: Int) {
        val seat = seats.firstOrNull { it.id == playerId } ?: return
        val updatedSeat = seat.copy(
            stars = newStars,
            chips = seat.chips + addedChips,
            isSittingOut = false
        )
        val idx = seats.indexOf(seat)
        seats[idx] = updatedSeat
        pushState()
    }

    @Synchronized
    fun toggleEscalatingBlinds() {
        val enabled = !state.escalatingBlinds
        state = state.copy(escalatingBlinds = enabled)
        pushState()
    }

    @Synchronized
    fun rabbitHunt() {
        if (state.phase == RoundPhase.SHOWDOWN && communityCards.size < 5) {
            while (communityCards.size < 5) {
                communityCards.add(deck.dealCard())
            }
            state = state.copy(communityCards = communityCards.toList())
            pushState()
        }
    }

    @Synchronized
    fun startNewHand() {
        val activePlayers = seats.filter { it.chips > 0 }
        if (activePlayers.size < 2) return

        deck.reset()
        communityCards.clear()
        playersActedInRound.clear()

        val nextHandCount = state.handCount + 1
        var curSb = state.smallBlind
        var curBb = state.bigBlind
        var curLvl = state.blindLevel

        if (state.escalatingBlinds && nextHandCount > 1 && (nextHandCount - 1) % 8 == 0) {
            curLvl++
            when (curLvl) {
                2 -> { curSb = 100; curBb = 200 }
                3 -> { curSb = 200; curBb = 400 }
                4 -> { curSb = 500; curBb = 1000 }
                else -> { curSb = curSb * 2; curBb = curBb * 2 }
            }
        }

        // Reset seats for new hand
        for (i in seats.indices) {
            val p = seats[i]
            if (p.chips <= 0) {
                p.isSittingOut = true
                p.cards = emptyList()
                p.folded = true
                p.isAllIn = false
            } else {
                p.isSittingOut = false
                p.folded = false
                p.isAllIn = false
                p.cards = listOf(deck.dealCard(), deck.dealCard())
            }
            p.roundBet = 0
            p.totalHandBet = 0
            p.lastActionText = null
        }

        // Advance dealer button
        var nextDealer = (state.dealerIndex + 1) % seats.size
        while (seats[nextDealer].chips <= 0) {
            nextDealer = (nextDealer + 1) % seats.size
        }

        // Determine blinds
        val eligibleSeats = seats.mapIndexedNotNull { index, p -> if (p.chips > 0) index else null }
        val dealerPosInEligible = eligibleSeats.indexOf(nextDealer)

        val sbIdx: Int
        val bbIdx: Int
        if (eligibleSeats.size == 2) {
            // Heads up: Dealer is SB, other is BB
            sbIdx = eligibleSeats[dealerPosInEligible]
            bbIdx = eligibleSeats[(dealerPosInEligible + 1) % 2]
        } else {
            sbIdx = eligibleSeats[(dealerPosInEligible + 1) % eligibleSeats.size]
            bbIdx = eligibleSeats[(dealerPosInEligible + 2) % eligibleSeats.size]
        }

        // Post Small Blind
        val sbPlayer = seats[sbIdx]
        val sbAmount = min(curSb, sbPlayer.chips)
        sbPlayer.chips -= sbAmount
        sbPlayer.roundBet = sbAmount
        sbPlayer.totalHandBet = sbAmount
        if (sbPlayer.chips == 0) sbPlayer.isAllIn = true
        sbPlayer.lastActionText = "SB ${CurrencyFormatter.formatChips(sbAmount)}"

        // Post Big Blind
        val bbPlayer = seats[bbIdx]
        val bbAmount = min(curBb, bbPlayer.chips)
        bbPlayer.chips -= bbAmount
        bbPlayer.roundBet = bbAmount
        bbPlayer.totalHandBet = bbAmount
        if (bbPlayer.chips == 0) bbPlayer.isAllIn = true
        bbPlayer.lastActionText = "BB ${CurrencyFormatter.formatChips(bbAmount)}"

        val highBet = max(sbAmount, bbAmount)
        val minRaise = curBb * 2

        // First to act pre-flop is player after BB (UTG)
        var firstActorIdx: Int = if (eligibleSeats.size == 2) {
            sbIdx // In heads-up, dealer acts first pre-flop
        } else {
            eligibleSeats[(eligibleSeats.indexOf(bbIdx) + 1) % eligibleSeats.size]
        }

        // If the initial actor was forced all-in by blind, advance to next player who can act
        if (seats[firstActorIdx].isAllIn) {
            var loop = 0
            while (loop < eligibleSeats.size && seats[firstActorIdx].isAllIn) {
                val nextPos = (eligibleSeats.indexOf(firstActorIdx) + 1) % eligibleSeats.size
                firstActorIdx = eligibleSeats[nextPos]
                loop++
            }
        }

        val canActCount = seats.count { !it.isSittingOut && !it.folded && !it.isAllIn }
        if (canActCount <= 1) {
            firstActorIdx = -1
        }

        lastRaiserIndex = bbIdx

        state = state.copy(
            phase = RoundPhase.PRE_FLOP,
            communityCards = emptyList(),
            mainPot = 0,
            sidePots = emptyList(),
            currentHighBet = highBet,
            minRaiseAmount = minRaise,
            smallBlind = curSb,
            bigBlind = curBb,
            blindLevel = curLvl,
            dealerIndex = nextDealer,
            smallBlindIndex = sbIdx,
            bigBlindIndex = bbIdx,
            activeTurnIndex = firstActorIdx,
            winners = emptyList(),
            handCount = nextHandCount
        )

        AppLogger.i("GameEngine", "Starting hand #$nextHandCount (SB: $curSb, BB: $curBb)")
        pushState()

        if (canActCount <= 1) {
            scope.launch(Dispatchers.Default) {
                delay(800)
                synchronized(this@PokerGameEngine) {
                    if (state.phase != RoundPhase.SHOWDOWN) {
                        advancePhase()
                        pushState()
                    }
                }
            }
        } else {
            triggerAiIfTurn()
        }
    }

    @Synchronized
    fun handleAction(playerId: String, action: ActionType, raiseToAmount: Int = 0): Boolean {
        if (state.phase == RoundPhase.WAITING_FOR_PLAYERS ||
            state.phase == RoundPhase.SHOWDOWN ||
            state.phase == RoundPhase.HAND_COMPLETE
        ) return false

        val turnPlayer = state.activeTurnPlayer ?: return false
        if (turnPlayer.id != playerId) return false

        AppLogger.i("GameEngine", "${turnPlayer.name} ($playerId) action: $action, amount=$raiseToAmount")

        when (action) {
            ActionType.FOLD -> {
                turnPlayer.folded = true
                turnPlayer.lastActionText = "FOLD"
            }

            ActionType.CHECK -> {
                if (turnPlayer.roundBet < state.currentHighBet) {
                    // Cannot check if there's a higher bet to call
                    return false
                }
                turnPlayer.lastActionText = "CHECK"
            }

            ActionType.CALL -> {
                val neededToCall = state.currentHighBet - turnPlayer.roundBet
                val callAmount = min(neededToCall, turnPlayer.chips)
                turnPlayer.chips -= callAmount
                turnPlayer.roundBet += callAmount
                turnPlayer.totalHandBet += callAmount
                if (turnPlayer.chips == 0) {
                    turnPlayer.isAllIn = true
                    turnPlayer.lastActionText = "CALL ALL-IN ${CurrencyFormatter.formatChips(turnPlayer.roundBet)}"
                } else {
                    turnPlayer.lastActionText = "CALL ${CurrencyFormatter.formatChips(callAmount)}"
                }
            }

            ActionType.BET, ActionType.RAISE -> {
                val targetBet = max(raiseToAmount, state.currentHighBet + state.bigBlind)
                val additionalNeeded = targetBet - turnPlayer.roundBet
                val actualAddition = min(additionalNeeded, turnPlayer.chips)
                turnPlayer.chips -= actualAddition
                turnPlayer.roundBet += actualAddition
                turnPlayer.totalHandBet += actualAddition

                if (turnPlayer.chips == 0) {
                    turnPlayer.isAllIn = true
                    turnPlayer.lastActionText = "ALL-IN ${CurrencyFormatter.formatChips(turnPlayer.roundBet)}"
                } else {
                    turnPlayer.lastActionText = "RAISE ${CurrencyFormatter.formatChips(turnPlayer.roundBet)}"
                }

                if (turnPlayer.roundBet > state.currentHighBet) {
                    state = state.copy(
                        currentHighBet = turnPlayer.roundBet,
                        minRaiseAmount = turnPlayer.roundBet + max(state.bigBlind, turnPlayer.roundBet - state.currentHighBet)
                    )
                    lastRaiserIndex = state.activeTurnIndex
                    playersActedInRound.clear() // Reopen betting
                }
            }

            ActionType.ALL_IN -> {
                val allInAmount = turnPlayer.chips
                turnPlayer.chips = 0
                turnPlayer.roundBet += allInAmount
                turnPlayer.totalHandBet += allInAmount
                turnPlayer.isAllIn = true
                turnPlayer.lastActionText = "ALL-IN ${CurrencyFormatter.formatChips(turnPlayer.roundBet)}"

                if (turnPlayer.roundBet > state.currentHighBet) {
                    state = state.copy(
                        currentHighBet = turnPlayer.roundBet,
                        minRaiseAmount = turnPlayer.roundBet + state.bigBlind
                    )
                    lastRaiserIndex = state.activeTurnIndex
                    playersActedInRound.clear()
                }
            }
        }

        playersActedInRound.add(turnPlayer.id)

        // Check if only one player remaining (everyone else folded)
        val activePlayers = seats.filter { !it.isSittingOut && !it.folded }
        if (activePlayers.size == 1) {
            handleSingleWinnerByFold(activePlayers.first())
            return true
        }

        // Check if betting round is complete
        if (isBettingRoundComplete()) {
            gatherBetsIntoPot()
            advancePhase()
        } else {
            advanceTurn()
        }

        pushState()
        triggerAiIfTurn()
        return true
    }

    private fun isBettingRoundComplete(): Boolean {
        val contenders = seats.filter { !it.isSittingOut && !it.folded }
        val canStillAct = contenders.filter { !it.isAllIn }

        if (canStillAct.isEmpty()) {
            return true // Everyone is all-in
        }

        // All non-all-in contenders must have acted and matched the high bet
        for (p in canStillAct) {
            if (!playersActedInRound.contains(p.id)) return false
            if (p.roundBet < state.currentHighBet) return false
        }
        return true
    }

    private fun gatherBetsIntoPot() {
        var totalRoundChips = 0
        for (p in seats) {
            totalRoundChips += p.roundBet
            p.roundBet = 0
        }
        state = state.copy(
            mainPot = state.mainPot + totalRoundChips,
            currentHighBet = 0,
            minRaiseAmount = state.bigBlind
        )
        playersActedInRound.clear()
    }

    private fun advanceTurn() {
        val activeCount = seats.count { !it.isSittingOut && !it.folded && !it.isAllIn }
        if (activeCount == 0) return

        var nextIdx = (state.activeTurnIndex + 1) % seats.size
        var loopCount = 0
        while (loopCount < seats.size) {
            val p = seats[nextIdx]
            if (!p.isSittingOut && !p.folded && !p.isAllIn) {
                state = state.copy(activeTurnIndex = nextIdx)
                return
            }
            nextIdx = (nextIdx + 1) % seats.size
            loopCount++
        }
    }

    @Synchronized
    private fun advancePhase() {
        val nonFolded = seats.filter { !it.isSittingOut && !it.folded }
        val canAct = nonFolded.filter { !it.isAllIn }

        when (state.phase) {
            RoundPhase.PRE_FLOP -> {
                // Deal Flop (3 cards)
                communityCards.add(deck.dealCard())
                communityCards.add(deck.dealCard())
                communityCards.add(deck.dealCard())
                state = state.copy(phase = RoundPhase.FLOP, communityCards = communityCards.toList())
            }

            RoundPhase.FLOP -> {
                // Deal Turn (1 card)
                communityCards.add(deck.dealCard())
                state = state.copy(phase = RoundPhase.TURN, communityCards = communityCards.toList())
            }

            RoundPhase.TURN -> {
                // Deal River (1 card)
                communityCards.add(deck.dealCard())
                state = state.copy(phase = RoundPhase.RIVER, communityCards = communityCards.toList())
            }

            RoundPhase.RIVER -> {
                resolveShowdown()
                return
            }

            else -> {}
        }

        // If at most 1 player can act (others all-in), run out remaining cards straight to showdown
        if (canAct.size <= 1) {
            state = state.copy(activeTurnIndex = -1)
            scope.launch(Dispatchers.Default) {
                delay(800)
                synchronized(this@PokerGameEngine) {
                    if (state.phase != RoundPhase.SHOWDOWN) {
                        advancePhase()
                        pushState()
                    }
                }
            }
            return
        }

        // Reset turn to first active player left of dealer
        var nextIdx = (state.dealerIndex + 1) % seats.size
        var loopCount = 0
        while (loopCount < seats.size && (seats[nextIdx].isSittingOut || seats[nextIdx].folded || seats[nextIdx].isAllIn)) {
            nextIdx = (nextIdx + 1) % seats.size
            loopCount++
        }
        if (loopCount < seats.size) {
            state = state.copy(activeTurnIndex = nextIdx)
        } else {
            state = state.copy(activeTurnIndex = -1)
        }
    }

    private fun handleSingleWinnerByFold(winner: PlayerSeat) {
        gatherBetsIntoPot()
        val totalAmount = state.mainPot
        winner.chips += totalAmount
        val winnerInfo = HandWinner(
            playerId = winner.id,
            playerName = winner.name,
            amountWon = totalAmount,
            handDescription = "All Opponents Folded",
            bestCards = winner.cards,
            potDescription = "Main Pot"
        )

        val history = LastHandHistory(
            handNumber = state.handCount,
            board = communityCards.toList(),
            pot = totalAmount,
            winners = listOf(winnerInfo),
            hands = listOf(RevealedHand(winner.name, winner.cards, "Winner by Fold"))
        )

        state = state.copy(
            phase = RoundPhase.SHOWDOWN,
            mainPot = 0,
            activeTurnIndex = -1,
            winners = listOf(winnerInfo),
            lastHand = history
        )
        AppLogger.i("GameEngine", "Hand #${state.handCount} won by fold: ${winner.name} won ₹$totalAmount")
        pushState()
    }

    @Synchronized
    private fun resolveShowdown() {
        val contenders = seats.filter { !it.isSittingOut && !it.folded }
        if (contenders.isEmpty()) return

        // Evaluate hands - Keyed by Player ID (immutable String), NOT PlayerSeat
        // Because PlayerSeat.chips is mutated during payout, which invalidates data class hashCode!
        val evaluations = contenders.associate { player ->
            player.id to HandEvaluator.evaluate(player.cards + communityCards)
        }

        // Calculate side pots based on totalHandBet
        val uniqueBets = contenders.map { it.totalHandBet }.filter { it > 0 }.distinct().sorted()
        val sidePots = mutableListOf<SidePot>()
        var coveredBet = 0

        for (betLevel in uniqueBets) {
            val incremental = betLevel - coveredBet
            if (incremental > 0) {
                val eligible = contenders.filter { it.totalHandBet >= betLevel }.map { it.id }
                // Include folded players' bets in the pot tier they contributed to
                val potSize = seats.sumOf { seat ->
                    max(0, min(seat.totalHandBet - coveredBet, incremental))
                }
                if (potSize > 0) {
                    sidePots.add(SidePot(potSize, eligible))
                }
                coveredBet = betLevel
            }
        }

        // Any excess unallocated chips in mainPot
        val allocatedSidePotsTotal = sidePots.sumOf { it.amount }
        val leftoverPot = max(0, state.mainPot - allocatedSidePotsTotal)

        val winnersList = mutableListOf<HandWinner>()

        if (sidePots.isEmpty()) {
            // Simple single pot
            val bestEval = evaluations.values.maxOrNull()
            if (bestEval != null) {
                val winningPlayers = contenders.filter { evaluations[it.id] == bestEval }
                if (winningPlayers.isNotEmpty()) {
                    val splitAmount = state.mainPot / winningPlayers.size
                    val remainder = state.mainPot % winningPlayers.size
                    for ((idx, w) in winningPlayers.withIndex()) {
                        val payout = splitAmount + if (idx == 0) remainder else 0
                        w.chips += payout
                        winnersList.add(
                            HandWinner(
                                w.id,
                                w.name,
                                payout,
                                bestEval.description,
                                bestEval.bestFiveCards,
                                potDescription = if (winningPlayers.size > 1) "Split Pot" else "Main Pot"
                            )
                        )
                    }
                }
            }
        } else {
            // Add any leftover pot to the first side pot (main pot)
            if (leftoverPot > 0 && sidePots.isNotEmpty()) {
                val firstPot = sidePots[0]
                sidePots[0] = SidePot(firstPot.amount + leftoverPot, firstPot.eligiblePlayerIds)
            }

            for ((potIdx, pot) in sidePots.withIndex()) {
                if (pot.amount <= 0) continue
                val eligibleContenders = contenders.filter { pot.eligiblePlayerIds.contains(it.id) }
                if (eligibleContenders.isNotEmpty()) {
                    val bestEval = eligibleContenders.mapNotNull { evaluations[it.id] }.maxOrNull()
                    if (bestEval != null) {
                        val potWinners = eligibleContenders.filter { evaluations[it.id] == bestEval }
                        if (potWinners.isNotEmpty()) {
                            val share = pot.amount / potWinners.size
                            val remainder = pot.amount % potWinners.size
                            val potLabel = if (potIdx == 0) {
                                "Main Pot"
                            } else if (sidePots.size == 2) {
                                "Side Pot"
                            } else {
                                "Side Pot #${potIdx}"
                            }
                            for ((idx, pw) in potWinners.withIndex()) {
                                val payout = share + if (idx == 0) remainder else 0
                                pw.chips += payout
                                winnersList.add(
                                    HandWinner(
                                        pw.id,
                                        pw.name,
                                        payout,
                                        bestEval.description,
                                        bestEval.bestFiveCards,
                                        potDescription = potLabel
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        val revealedList = contenders.map {
            val eval = evaluations[it.id]
            RevealedHand(it.name, it.cards, eval?.description ?: "High Card")
        }

        val history = LastHandHistory(
            handNumber = state.handCount,
            board = communityCards.toList(),
            pot = winnersList.sumOf { it.amountWon },
            winners = winnersList,
            hands = revealedList
        )

        state = state.copy(
            phase = RoundPhase.SHOWDOWN,
            mainPot = 0,
            sidePots = emptyList(),
            activeTurnIndex = -1,
            winners = winnersList,
            lastHand = history
        )
        AppLogger.i("GameEngine", "Hand #${state.handCount} showdown: ${winnersList.joinToString { "${it.playerName} won ₹${it.amountWon} (${it.handDescription})" }}")
        pushState()
    }

    private fun triggerAiIfTurn() {
        if (state.phase == RoundPhase.SHOWDOWN || state.activeTurnIndex == -1) return
        val active = state.activeTurnPlayer ?: return
        if (active.isAi && !active.folded && !active.isAllIn) {
            scope.launch(Dispatchers.Default) {
                // Human-like thought time variation:
                // Fast snap on check (750-1200ms), deeper thought when facing bets/raises (1300-2200ms)
                val needed = state.currentHighBet - active.roundBet
                val thinkMs = if (needed == 0) {
                    (750 + Math.random() * 450).toLong()
                } else {
                    (1300 + Math.random() * 900).toLong()
                }
                delay(thinkMs)
                synchronized(this@PokerGameEngine) {
                    if (state.activeTurnIndex != -1 &&
                        state.phase != RoundPhase.SHOWDOWN &&
                        state.activeTurnPlayer?.id == active.id &&
                        !active.folded && !active.isAllIn
                    ) {
                        decideAiAction(active)
                    }
                }
            }
        }
    }

    private fun decideAiAction(ai: PlayerSeat) {
        if (ai.cards.isEmpty() || ai.folded || ai.isAllIn) return

        val neededToCall = state.currentHighBet - ai.roundBet
        val canCheck = neededToCall <= 0
        val mySeatIndex = state.seats.indexOf(ai)
        val isButton = state.dealerIndex == mySeatIndex
        val isLatePosition = isButton || ((state.dealerIndex - 1 + state.seats.size) % state.seats.size == mySeatIndex)
        val pot = max(1, state.totalPot)
        val potOdds = neededToCall.toDouble() / (pot + neededToCall)

        // Deterministic, balanced bot personality derived from bot name hash
        val botHash = Math.abs(ai.name.hashCode())
        val style = botHash % 4
        val isTag = style == 0 // Tight-Aggressive
        val isLag = style == 1 // Loose-Aggressive
        val isStation = style == 2 // Calling Station
        val isBalanced = style == 3 // Balanced / Tricky

        val rng = Math.random()

        if (state.phase == RoundPhase.PRE_FLOP) {
            decidePreFlopAction(ai, neededToCall, canCheck, isLatePosition, isTag, isLag, isStation, isBalanced, potOdds, rng)
        } else {
            decidePostFlopAction(ai, neededToCall, canCheck, isLatePosition, isTag, isLag, isStation, isBalanced, potOdds, rng)
        }
    }

    /**
     * Rounds bet amounts to clean, natural poker numbers (multiples of 50 or big blind).
     * Eliminates weird unrounded amounts like 115, 183, 237.
     */
    private fun roundToCleanBet(rawAmount: Int): Int {
        val bb = max(10, state.bigBlind)
        val step = if (bb >= 100) 50 else max(10, bb)
        val rounded = ((rawAmount + step / 2) / step) * step
        return max(bb, rounded)
    }

    private fun hasStraightDraw(cards: List<Card>): Boolean {
        val ranks = cards.map { it.rank.value }.distinct().sorted()
        if (ranks.size < 4) return false
        for (i in 0..ranks.size - 4) {
            if (ranks[i + 3] - ranks[i] <= 4) return true
        }
        if (ranks.contains(14)) {
            val aceLow = (listOf(1) + ranks.filter { it < 14 }).distinct().sorted()
            for (i in 0..aceLow.size - 4) {
                if (aceLow[i + 3] - aceLow[i] <= 4) return true
            }
        }
        return false
    }

    private fun decidePreFlopAction(
        ai: PlayerSeat,
        neededToCall: Int,
        canCheck: Boolean,
        isLatePos: Boolean,
        isTag: Boolean,
        isLag: Boolean,
        isStation: Boolean,
        isBalanced: Boolean,
        potOdds: Double,
        rng: Double
    ) {
        val c1 = ai.cards[0]
        val c2 = ai.cards[1]
        val high = max(c1.rank.value, c2.rank.value)
        val low = min(c1.rank.value, c2.rank.value)
        val isPair = c1.rank == c2.rank
        val isSuited = c1.suit == c2.suit
        val gap = high - low
        val maxBet = ai.roundBet + ai.chips

        // Hand strength tier (1 = Monster, 2 = Strong, 3 = Speculative/Playable, 4 = Marginal Defend, 5 = Trash)
        val tier = when {
            isPair && high >= 10 -> 1 // TT, JJ, QQ, KK, AA
            high == 14 && low >= 13 -> 1 // AKs, AKo
            isPair && high >= 7 -> 2 // 77, 88, 99
            high == 14 && (low >= 11 || (isSuited && low >= 10)) -> 2 // AQ, AJ, ATs
            high == 13 && (low >= 12 || (isSuited && low >= 11)) -> 2 // KQ, KJs, QJs
            isPair -> 3 // 22 - 66
            isSuited && high == 14 -> 3 // A2s - A9s
            isSuited && gap <= 2 && high >= 6 -> 3 // Suited connectors (65s to JTs)
            isSuited && gap <= 3 && high >= 9 -> 3 // Suited one/two-gappers (e.g. T8s, J9s, QTs)
            high >= 11 && low >= 10 -> 3 // QJ, KJ, JT
            high == 14 -> 4 // A2o - A9o
            high == 13 && low >= 8 -> 4 // K8o - KTo
            high >= 11 && (isSuited || gap <= 2) -> 4 // broadway/connected
            isStation || isLag -> 4
            else -> 5 // Pure Trash
        }

        if (canCheck) {
            when (tier) {
                1 -> {
                    // Open raise or 3-bet to 3x BB in clean numbers
                    val raiseAmt = roundToCleanBet(state.bigBlind * 3)
                    handleAction(ai.id, ActionType.RAISE, min(maxBet, raiseAmt))
                }
                2 -> {
                    if (rng < (if (isLag) 0.85 else 0.70)) {
                        val raiseAmt = roundToCleanBet((state.bigBlind * 2.5).toInt())
                        handleAction(ai.id, ActionType.RAISE, min(maxBet, raiseAmt))
                    } else {
                        handleAction(ai.id, ActionType.CHECK)
                    }
                }
                3 -> {
                    if (isLatePos && rng < (if (isLag) 0.55 else 0.35)) {
                        val raiseAmt = roundToCleanBet(state.bigBlind * 2)
                        handleAction(ai.id, ActionType.RAISE, min(maxBet, raiseAmt))
                    } else {
                        handleAction(ai.id, ActionType.CHECK)
                    }
                }
                else -> {
                    handleAction(ai.id, ActionType.CHECK)
                }
            }
        } else {
            // Facing a bet or raise preflop
            val callMultiple = neededToCall.toDouble() / max(1, state.bigBlind)
            when (tier) {
                1 -> {
                    // Premium hands: Re-raise (3-bet) 75% or flat call trap 25%
                    if (rng < 0.75 && !isStation) {
                        val reRaise = roundToCleanBet(state.currentHighBet * 3)
                        handleAction(ai.id, ActionType.RAISE, min(maxBet, reRaise))
                    } else {
                        handleAction(ai.id, ActionType.CALL)
                    }
                }
                2 -> {
                    // Strong hands: NEVER fold to a single open raise!
                    if (callMultiple <= 3.5 || isStation || isLag) {
                        if (rng < 0.25 && !isStation && callMultiple <= 3.0) {
                            val reRaise = roundToCleanBet(state.currentHighBet + state.bigBlind * 2)
                            handleAction(ai.id, ActionType.RAISE, min(maxBet, reRaise))
                        } else {
                            handleAction(ai.id, ActionType.CALL)
                        }
                    } else {
                        // Facing large 4-bet+
                        if (rng < 0.50) handleAction(ai.id, ActionType.CALL) else handleAction(ai.id, ActionType.FOLD)
                    }
                }
                3 -> {
                    // Speculative/Playable: Call standard opens (up to 3x BB), fold only to huge re-raises
                    if (callMultiple <= 3.2 || isStation || (isLatePos && callMultiple <= 4.0)) {
                        handleAction(ai.id, ActionType.CALL)
                    } else {
                        handleAction(ai.id, ActionType.FOLD)
                    }
                }
                4 -> {
                    // Defending hands: Defend blinds or late position against standard min-bets
                    val isBlind = state.smallBlindIndex == state.seats.indexOf(ai) || state.bigBlindIndex == state.seats.indexOf(ai)
                    if ((isBlind && callMultiple <= 2.2) || (isLatePos && callMultiple <= 2.0) || (isStation && callMultiple <= 2.5)) {
                        handleAction(ai.id, ActionType.CALL)
                    } else {
                        handleAction(ai.id, ActionType.FOLD)
                    }
                }
                else -> {
                    // Trash hands: Complete in BB if only 1 small blind or free
                    if (neededToCall <= state.smallBlind && rng < 0.50) {
                        handleAction(ai.id, ActionType.CALL)
                    } else {
                        handleAction(ai.id, ActionType.FOLD)
                    }
                }
            }
        }
    }

    private fun decidePostFlopAction(
        ai: PlayerSeat,
        neededToCall: Int,
        canCheck: Boolean,
        isLatePos: Boolean,
        isTag: Boolean,
        isLag: Boolean,
        isStation: Boolean,
        isBalanced: Boolean,
        potOdds: Double,
        rng: Double
    ) {
        val allCards = ai.cards + state.communityCards
        val eval = HandEvaluator.evaluate(allCards)
        val handRank = eval.handType.rankValue
        val maxBet = ai.roundBet + ai.chips

        // Draws & Texture
        val suitCounts = allCards.groupBy { it.suit }.mapValues { it.value.size }
        val hasFlushDraw = suitCounts.any { it.value == 4 }
        val hasStraightDraw = hasStraightDraw(allCards)

        val boardRanks = state.communityCards.map { it.rank.value }.sortedDescending()
        val boardHigh = boardRanks.firstOrNull() ?: 14
        val holeHigh = max(ai.cards[0].rank.value, ai.cards[1].rank.value)
        val holeLow = min(ai.cards[0].rank.value, ai.cards[1].rank.value)
        val isPocketPair = ai.cards[0].rank == ai.cards[1].rank

        val isTopPairOrOverpair = handRank >= HandType.TWO_PAIR.rankValue ||
                (handRank == HandType.ONE_PAIR.rankValue && (holeHigh >= boardHigh || (isPocketPair && holeHigh > boardHigh)))

        val isMiddlePair = handRank == HandType.ONE_PAIR.rankValue && !isTopPairOrOverpair &&
                (boardRanks.size >= 2 && holeHigh >= boardRanks[1])

        val hasOvercards = holeLow > boardHigh

        val pot = max(state.bigBlind * 2, state.totalPot)

        if (canCheck) {
            when {
                // Monster Hands (Three of a Kind, Straight, Flush, Full House+)
                handRank >= HandType.THREE_OF_A_KIND.rankValue -> {
                    // Value bet 1/2 to 2/3 pot in clean numbers (or trap 20% on Flop)
                    if (state.phase == RoundPhase.FLOP && rng < 0.20 && (isTag || isBalanced)) {
                        handleAction(ai.id, ActionType.CHECK)
                    } else {
                        val betAmt = roundToCleanBet((pot * 0.60).toInt())
                        handleAction(ai.id, ActionType.BET, min(maxBet, betAmt))
                    }
                }

                // Top Pair / Overpair / Two Pair
                isTopPairOrOverpair -> {
                    if (rng < (if (isTag) 0.85 else 0.75)) {
                        val betAmt = roundToCleanBet((pot * 0.50).toInt())
                        handleAction(ai.id, ActionType.BET, min(maxBet, betAmt))
                    } else {
                        handleAction(ai.id, ActionType.CHECK)
                    }
                }

                // Middle / Second Pair
                isMiddlePair -> {
                    if (isLatePos && rng < (if (isLag) 0.50 else 0.30)) {
                        val betAmt = roundToCleanBet((pot * 0.35).toInt())
                        handleAction(ai.id, ActionType.BET, min(maxBet, betAmt))
                    } else {
                        handleAction(ai.id, ActionType.CHECK)
                    }
                }

                // Strong Draws (Flush Draw or Straight Draw)
                (hasFlushDraw || hasStraightDraw) && state.phase != RoundPhase.RIVER -> {
                    // Semi-bluff bet 40% of time
                    if (rng < (if (isLag) 0.60 else 0.40)) {
                        val betAmt = roundToCleanBet((pot * 0.40).toInt())
                        handleAction(ai.id, ActionType.BET, min(maxBet, betAmt))
                    } else {
                        handleAction(ai.id, ActionType.CHECK)
                    }
                }

                // Overcards or Air in Late Position (C-Bet / Probe Bluff)
                else -> {
                    val canBluff = (hasOvercards && rng < 0.45) || (isLatePos && rng < (if (isLag) 0.40 else 0.22))
                    if (canBluff) {
                        val betAmt = roundToCleanBet((pot * 0.35).toInt())
                        handleAction(ai.id, ActionType.BET, min(maxBet, betAmt))
                    } else {
                        handleAction(ai.id, ActionType.CHECK)
                    }
                }
            }
        } else {
            // Facing a bet or raise post-flop
            val callRatioToPot = neededToCall.toDouble() / pot
            when {
                // Monster Hands (Full House, Quads, Flush, Straight, Trips)
                handRank >= HandType.THREE_OF_A_KIND.rankValue -> {
                    // NEVER fold! Raise 40% of the time, Call 60%
                    if (rng < 0.40 && !isStation) {
                        val raiseAmt = roundToCleanBet(state.currentHighBet + max(state.bigBlind * 2, (pot * 0.60).toInt()))
                        handleAction(ai.id, ActionType.RAISE, min(maxBet, raiseAmt))
                    } else {
                        handleAction(ai.id, ActionType.CALL)
                    }
                }

                // Top Pair / Overpair / Two Pair
                isTopPairOrOverpair -> {
                    // Human players DO NOT fold Top Pair to standard bets!
                    if (callRatioToPot > 1.2 && state.phase == RoundPhase.RIVER && !isStation && rng < 0.45) {
                        // Massive river over-bet: occasional fold
                        handleAction(ai.id, ActionType.FOLD)
                    } else if (rng < 0.20 && (isLag || isTag) && callRatioToPot <= 0.6) {
                        val raiseAmt = roundToCleanBet(state.currentHighBet + (pot * 0.50).toInt())
                        handleAction(ai.id, ActionType.RAISE, min(maxBet, raiseAmt))
                    } else {
                        handleAction(ai.id, ActionType.CALL)
                    }
                }

                // Middle Pair / Second Pair
                isMiddlePair -> {
                    // Call on Flop 80%, Turn 60%, River 40% (Solid bluff catchers!)
                    val callProb = when (state.phase) {
                        RoundPhase.FLOP -> if (callRatioToPot <= 0.8) 0.85 else 0.60
                        RoundPhase.TURN -> if (callRatioToPot <= 0.7) 0.65 else 0.40
                        RoundPhase.RIVER -> if (callRatioToPot <= 0.5) 0.45 else 0.20
                        else -> 0.50
                    }
                    if (isStation || rng < callProb) {
                        handleAction(ai.id, ActionType.CALL)
                    } else {
                        handleAction(ai.id, ActionType.FOLD)
                    }
                }

                // Weak / Bottom Pair
                handRank == HandType.ONE_PAIR.rankValue -> {
                    // Peel on flop if bet is reasonable
                    val canCall = when (state.phase) {
                        RoundPhase.FLOP -> callRatioToPot <= 0.55 || isStation
                        RoundPhase.TURN -> callRatioToPot <= 0.35 || isStation
                        else -> isStation && callRatioToPot <= 0.25
                    }
                    if (canCall && (rng < 0.65 || isStation)) {
                        handleAction(ai.id, ActionType.CALL)
                    } else {
                        handleAction(ai.id, ActionType.FOLD)
                    }
                }

                // Flush Draw or Straight Draw (Live Outs!)
                (hasFlushDraw || hasStraightDraw) && state.phase != RoundPhase.RIVER -> {
                    // Real poker players call draws with good pot odds and implied odds
                    val drawThreshold = if (hasFlushDraw) 0.85 else 0.65
                    if (callRatioToPot <= drawThreshold || isStation || (isLag && rng < 0.75)) {
                        // Occasional semi-bluff raise on Flop
                        if (state.phase == RoundPhase.FLOP && rng < (if (isLag) 0.30 else 0.15) && callRatioToPot <= 0.5) {
                            val raiseAmt = roundToCleanBet(state.currentHighBet + (pot * 0.50).toInt())
                            handleAction(ai.id, ActionType.RAISE, min(maxBet, raiseAmt))
                        } else {
                            handleAction(ai.id, ActionType.CALL)
                        }
                    } else {
                        handleAction(ai.id, ActionType.FOLD)
                    }
                }

                // Two Overcards (e.g. A-K / A-Q with no pair on 8-5-2 board)
                hasOvercards && state.phase == RoundPhase.FLOP && callRatioToPot <= 0.45 -> {
                    // Float 1 street with 6 outs to top pair top kicker!
                    if (rng < 0.60 || isStation || isLag) {
                        handleAction(ai.id, ActionType.CALL)
                    } else {
                        handleAction(ai.id, ActionType.FOLD)
                    }
                }

                // Nothing / Air
                else -> {
                    if (isStation && neededToCall <= state.bigBlind && rng < 0.35) {
                        handleAction(ai.id, ActionType.CALL)
                    } else {
                        handleAction(ai.id, ActionType.FOLD)
                    }
                }
            }
        }
    }

    private fun pushState() {
        state = state.copy(seats = seats.toList())
        onStateUpdated(state)
    }
}
