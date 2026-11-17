package com.example.offsuitpoker.data

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

data class PlayerProfile(
    val playerId: String,
    val playerName: String,
    val starsRemaining: Int,      // Out of 5 lifetime stars
    val currentChips: Int,        // Current chips in hand/wallet for real LAN tables
    val totalBoughtInAmount: Int, // Max 50,000 over lifetime
    val totalBuyInsCount: Int,    // Max 5 over lifetime
    val fourColorDeckEnabled: Boolean = false,
    val practiceChips: Int = 10000 // Completely isolated bankroll for Solo AI practice!
) {
    val isPermanentlyBroke: Boolean
        get() = starsRemaining == 0 && currentChips <= 0

    val starsDisplay: String
        get() = "★".repeat(starsRemaining.coerceIn(0, 5))

    val canRebuy: Boolean
        get() = starsRemaining > 0
}

class ProfileManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("offsuit_poker_prefs", Context.MODE_PRIVATE)

    companion object {
        const val MAX_LIFETIME_STARS = 5
        const val BUY_IN_AMOUNT_PER_STAR = 10000
        const val MAX_LIFETIME_CHIPS = MAX_LIFETIME_STARS * BUY_IN_AMOUNT_PER_STAR // 50,000

        private const val KEY_PLAYER_ID = "player_id"
        private const val KEY_PLAYER_NAME = "player_name"
        private const val KEY_STARS = "stars_remaining"
        private const val KEY_CURRENT_CHIPS = "current_chips"
        private const val KEY_TOTAL_BOUGHT_IN = "total_bought_in"
        private const val KEY_BUY_IN_COUNT = "buy_in_count"
        private const val KEY_FIRST_RUN_DONE = "first_run_done"
        private const val KEY_FOUR_COLOR = "four_color_deck"
        private const val KEY_PRACTICE_CHIPS = "practice_chips"
    }

    init {
        // Initialize if first run ever, or reset if requested
        if (!prefs.getBoolean(KEY_FIRST_RUN_DONE, false)) {
            val randomId = UUID.randomUUID().toString().take(8)
            prefs.edit()
                .putString(KEY_PLAYER_ID, "P-$randomId")
                .putString(KEY_PLAYER_NAME, "Player")
                .putInt(KEY_STARS, MAX_LIFETIME_STARS)
                .putInt(KEY_CURRENT_CHIPS, 0)
                .putInt(KEY_TOTAL_BOUGHT_IN, 0)
                .putInt(KEY_BUY_IN_COUNT, 0)
                .putInt(KEY_PRACTICE_CHIPS, 10000)
                .putBoolean(KEY_FOUR_COLOR, false)
                .putBoolean(KEY_FIRST_RUN_DONE, true)
                .apply()
        }
    }

    fun resetStarsTo5() {
        prefs.edit()
            .putInt(KEY_STARS, MAX_LIFETIME_STARS)
            .putInt(KEY_CURRENT_CHIPS, 0)
            .putInt(KEY_TOTAL_BOUGHT_IN, 0)
            .putInt(KEY_BUY_IN_COUNT, 0)
            .apply()
    }

    fun getProfile(): PlayerProfile {
        return PlayerProfile(
            playerId = prefs.getString(KEY_PLAYER_ID, "P-1001") ?: "P-1001",
            playerName = prefs.getString(KEY_PLAYER_NAME, "Player") ?: "Player",
            starsRemaining = prefs.getInt(KEY_STARS, MAX_LIFETIME_STARS),
            currentChips = prefs.getInt(KEY_CURRENT_CHIPS, 0),
            totalBoughtInAmount = prefs.getInt(KEY_TOTAL_BOUGHT_IN, 0),
            totalBuyInsCount = prefs.getInt(KEY_BUY_IN_COUNT, 0),
            fourColorDeckEnabled = prefs.getBoolean(KEY_FOUR_COLOR, false),
            practiceChips = prefs.getInt(KEY_PRACTICE_CHIPS, 10000)
        )
    }

    fun updatePlayerName(newName: String) {
        prefs.edit().putString(KEY_PLAYER_NAME, newName.trim().ifEmpty { "Player" }).apply()
    }

    fun toggleFourColorDeck(): Boolean {
        val current = prefs.getBoolean(KEY_FOUR_COLOR, false)
        val newValue = !current
        prefs.edit().putBoolean(KEY_FOUR_COLOR, newValue).apply()
        return newValue
    }

    /**
     * Executes a buy-in by spending 1 star from the player's lifetime allowance of 5 stars.
     * Returns true if successful, false if no stars remain.
     */
    @Synchronized
    fun buyInWithStar(): Boolean {
        val currentStars = prefs.getInt(KEY_STARS, 0)
        if (currentStars <= 0) return false

        val newStars = currentStars - 1
        val newChips = prefs.getInt(KEY_CURRENT_CHIPS, 0) + BUY_IN_AMOUNT_PER_STAR
        val totalBought = prefs.getInt(KEY_TOTAL_BOUGHT_IN, 0) + BUY_IN_AMOUNT_PER_STAR
        val buyInCount = prefs.getInt(KEY_BUY_IN_COUNT, 0) + 1

        prefs.edit()
            .putInt(KEY_STARS, newStars)
            .putInt(KEY_CURRENT_CHIPS, newChips)
            .putInt(KEY_TOTAL_BOUGHT_IN, totalBought)
            .putInt(KEY_BUY_IN_COUNT, buyInCount)
            .apply()
        return true
    }

    @Synchronized
    fun updateChips(chips: Int) {
        prefs.edit().putInt(KEY_CURRENT_CHIPS, chips.coerceAtLeast(0)).apply()
    }

    @Synchronized
    fun updatePracticeChips(chips: Int) {
        prefs.edit().putInt(KEY_PRACTICE_CHIPS, chips.coerceAtLeast(0)).apply()
    }

    @Synchronized
    fun refillPracticeChips() {
        prefs.edit().putInt(KEY_PRACTICE_CHIPS, 10000).apply()
    }
}
