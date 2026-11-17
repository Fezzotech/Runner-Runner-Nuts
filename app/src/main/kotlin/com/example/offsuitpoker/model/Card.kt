package com.example.offsuitpoker.model

import java.io.Serializable

enum class Suit(val symbol: String, val displayName: String) {
    HEARTS("♥", "Hearts"),
    DIAMONDS("♦", "Diamonds"),
    CLUBS("♣", "Clubs"),
    SPADES("♠", "Spades")
}

enum class Rank(val value: Int, val display: String) {
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K"),
    ACE(14, "A")
}

data class Card(
    val rank: Rank,
    val suit: Suit
) : Serializable, Comparable<Card> {
    override fun compareTo(other: Card): Int {
        return this.rank.value.compareTo(other.rank.value)
    }

    override fun toString(): String {
        return "${rank.display}${suit.symbol}"
    }

    fun toCode(): String {
        val s = when (suit) {
            Suit.HEARTS -> "h"
            Suit.DIAMONDS -> "d"
            Suit.CLUBS -> "c"
            Suit.SPADES -> "s"
        }
        return "${rank.display}$s"
    }

    companion object {
        fun fromCode(code: String): Card? {
            if (code.length < 2) return null
            val suitChar = code.last()
            val rankStr = code.dropLast(1)
            val suit = when (suitChar) {
                'h' -> Suit.HEARTS
                'd' -> Suit.DIAMONDS
                'c' -> Suit.CLUBS
                's' -> Suit.SPADES
                else -> return null
            }
            val rank = Rank.values().firstOrNull { it.display.equals(rankStr, ignoreCase = true) } ?: return null
            return Card(rank, suit)
        }
    }
}

class Deck {
    private val cards = mutableListOf<Card>()

    init {
        reset()
    }

    fun reset() {
        cards.clear()
        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                cards.add(Card(rank, suit))
            }
        }
        shuffle()
    }

    fun shuffle() {
        cards.shuffle()
    }

    fun dealCard(): Card {
        if (cards.isEmpty()) {
            reset()
        }
        return cards.removeAt(cards.size - 1)
    }

    val remainingCards: Int
        get() = cards.size
}
