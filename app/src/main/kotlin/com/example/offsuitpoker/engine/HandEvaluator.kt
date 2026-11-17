package com.example.offsuitpoker.engine

import com.example.offsuitpoker.model.Card
import com.example.offsuitpoker.model.Rank
import com.example.offsuitpoker.model.Suit

enum class HandType(val rankValue: Int, val displayName: String) {
    HIGH_CARD(1, "High Card"),
    ONE_PAIR(2, "One Pair"),
    TWO_PAIR(3, "Two Pair"),
    THREE_OF_A_KIND(4, "Three of a Kind"),
    STRAIGHT(5, "Straight"),
    FLUSH(6, "Flush"),
    FULL_HOUSE(7, "Full House"),
    FOUR_OF_A_KIND(8, "Four of a Kind"),
    STRAIGHT_FLUSH(9, "Straight Flush"),
    ROYAL_FLUSH(10, "Royal Flush")
}

data class HandEvaluation(
    val handType: HandType,
    val description: String,
    val tieBreakerScore: Long,
    val bestFiveCards: List<Card>
) : Comparable<HandEvaluation> {
    override fun compareTo(other: HandEvaluation): Int {
        if (this.handType.rankValue != other.handType.rankValue) {
            return this.handType.rankValue.compareTo(other.handType.rankValue)
        }
        return this.tieBreakerScore.compareTo(other.tieBreakerScore)
    }
}

object HandEvaluator {

    /**
     * Evaluates hand description for any stage of the game:
     * - Preflop (2 cards): Pocket pair -> "Pair of X", otherwise "High Card"
     * - Postflop (5 to 7 cards): Full 5-card best poker hand evaluation
     */
    fun evaluateLiveHand(holeCards: List<Card>, communityCards: List<Card>): String {
        if (holeCards.size < 2) return ""
        val allCards = holeCards + communityCards
        if (allCards.size < 5) {
            return if (holeCards[0].rank == holeCards[1].rank) {
                "Pair of ${holeCards[0].rank.display}s"
            } else {
                "High Card"
            }
        }
        val eval = evaluate(allCards)
        return when (eval.handType) {
            HandType.HIGH_CARD -> "High Card"
            HandType.ONE_PAIR -> eval.description
            HandType.TWO_PAIR -> "Two Pair"
            HandType.THREE_OF_A_KIND -> "Three of a Kind"
            HandType.STRAIGHT -> "Straight"
            HandType.FLUSH -> "Flush"
            HandType.FULL_HOUSE -> "Full House"
            HandType.FOUR_OF_A_KIND -> "Four of a Kind"
            HandType.STRAIGHT_FLUSH -> "Straight Flush"
            HandType.ROYAL_FLUSH -> "Royal Flush"
        }
    }

    fun evaluate(cards: List<Card>): HandEvaluation {
        if (cards.size < 5) {
            val sorted = cards.sortedDescending()
            return HandEvaluation(
                HandType.HIGH_CARD,
                "Incomplete",
                calculateScore(HandType.HIGH_CARD, sorted.map { it.rank.value }),
                sorted
            )
        }

        // Generate all 5-card combinations
        val combinations = get5CardCombinations(cards)
        var bestEval: HandEvaluation? = null

        for (comb in combinations) {
            val eval = evaluateExact5(comb)
            if (bestEval == null || eval > bestEval) {
                bestEval = eval
            }
        }

        return bestEval ?: evaluateExact5(cards.take(5))
    }

    private fun get5CardCombinations(cards: List<Card>): List<List<Card>> {
        val result = mutableListOf<List<Card>>()
        val n = cards.size
        for (i in 0 until n) {
            for (j in i + 1 until n) {
                for (k in j + 1 until n) {
                    for (l in k + 1 until n) {
                        for (m in l + 1 until n) {
                            result.add(listOf(cards[i], cards[j], cards[k], cards[l], cards[m]))
                        }
                    }
                }
            }
        }
        return result
    }

    private fun evaluateExact5(fiveCards: List<Card>): HandEvaluation {
        val sorted = fiveCards.sortedDescending()
        val isFlush = sorted.all { it.suit == sorted[0].suit }

        // Check for straight
        val straightResult = checkStraight(sorted)
        val isStraight = straightResult.isStraight
        val straightHigh = straightResult.highRank

        // Flush & Straight combinations
        if (isFlush && isStraight) {
            if (straightHigh == Rank.ACE.value) {
                return HandEvaluation(
                    HandType.ROYAL_FLUSH,
                    "Royal Flush",
                    calculateScore(HandType.ROYAL_FLUSH, listOf(straightHigh)),
                    sorted
                )
            } else {
                val rankName = Rank.values().first { it.value == straightHigh }.display
                return HandEvaluation(
                    HandType.STRAIGHT_FLUSH,
                    "Straight Flush ($rankName High)",
                    calculateScore(HandType.STRAIGHT_FLUSH, listOf(straightHigh)),
                    sorted
                )
            }
        }

        // Rank frequencies
        val countsByRank = sorted.groupBy { it.rank }.mapValues { it.value.size }
        val sortedCounts = countsByRank.entries.sortedWith(
            compareByDescending<Map.Entry<Rank, Int>> { it.value }.thenByDescending { it.key.value }
        )

        // Four of a kind
        if (sortedCounts[0].value == 4) {
            val quadRank = sortedCounts[0].key
            val kicker = sortedCounts[1].key
            return HandEvaluation(
                HandType.FOUR_OF_A_KIND,
                "Four of a Kind (${quadRank.display}s)",
                calculateScore(HandType.FOUR_OF_A_KIND, listOf(quadRank.value, kicker.value)),
                sorted
            )
        }

        // Full house
        if (sortedCounts[0].value == 3 && sortedCounts[1].value == 2) {
            val tripsRank = sortedCounts[0].key
            val pairRank = sortedCounts[1].key
            return HandEvaluation(
                HandType.FULL_HOUSE,
                "Full House (${tripsRank.display}s full of ${pairRank.display}s)",
                calculateScore(HandType.FULL_HOUSE, listOf(tripsRank.value, pairRank.value)),
                sorted
            )
        }

        // Flush
        if (isFlush) {
            val highRanks = sorted.map { it.rank.value }
            return HandEvaluation(
                HandType.FLUSH,
                "Flush (${sorted[0].rank.display} High)",
                calculateScore(HandType.FLUSH, highRanks),
                sorted
            )
        }

        // Straight
        if (isStraight) {
            val rankName = Rank.values().first { it.value == straightHigh }.display
            return HandEvaluation(
                HandType.STRAIGHT,
                "Straight ($rankName High)",
                calculateScore(HandType.STRAIGHT, listOf(straightHigh)),
                sorted
            )
        }

        // Three of a kind
        if (sortedCounts[0].value == 3) {
            val tripsRank = sortedCounts[0].key
            val kickers = listOf(sortedCounts[1].key.value, sortedCounts[2].key.value)
            return HandEvaluation(
                HandType.THREE_OF_A_KIND,
                "Three of a Kind (${tripsRank.display}s)",
                calculateScore(HandType.THREE_OF_A_KIND, listOf(tripsRank.value) + kickers),
                sorted
            )
        }

        // Two pair
        if (sortedCounts[0].value == 2 && sortedCounts[1].value == 2) {
            val highPair = sortedCounts[0].key
            val lowPair = sortedCounts[1].key
            val kicker = sortedCounts[2].key
            return HandEvaluation(
                HandType.TWO_PAIR,
                "Two Pair (${highPair.display}s and ${lowPair.display}s)",
                calculateScore(HandType.TWO_PAIR, listOf(highPair.value, lowPair.value, kicker.value)),
                sorted
            )
        }

        // One pair
        if (sortedCounts[0].value == 2) {
            val pairRank = sortedCounts[0].key
            val kickers = sortedCounts.drop(1).map { it.key.value }
            return HandEvaluation(
                HandType.ONE_PAIR,
                "Pair of ${pairRank.display}s",
                calculateScore(HandType.ONE_PAIR, listOf(pairRank.value) + kickers),
                sorted
            )
        }

        // High card
        val ranks = sorted.map { it.rank.value }
        return HandEvaluation(
            HandType.HIGH_CARD,
            "High Card ${sorted[0].rank.display}",
            calculateScore(HandType.HIGH_CARD, ranks),
            sorted
        )
    }

    private data class StraightCheck(val isStraight: Boolean, val highRank: Int)

    private fun checkStraight(sortedCards: List<Card>): StraightCheck {
        val uniqueRanks = sortedCards.map { it.rank.value }.distinct()
        if (uniqueRanks.size != 5) return StraightCheck(false, 0)

        // Normal straight
        if (uniqueRanks[0] - uniqueRanks[4] == 4) {
            return StraightCheck(true, uniqueRanks[0])
        }

        // Ace-low wheel straight: A, 5, 4, 3, 2
        if (uniqueRanks == listOf(Rank.ACE.value, 5, 4, 3, 2)) {
            return StraightCheck(true, 5) // 5 is the high card of wheel straight
        }

        return StraightCheck(false, 0)
    }

    private fun calculateScore(handType: HandType, ranks: List<Int>): Long {
        // Base weight in 16^x to avoid collision
        var score = handType.rankValue.toLong()
        for (r in ranks) {
            score = (score shl 4) + r
        }
        return score
    }
}
