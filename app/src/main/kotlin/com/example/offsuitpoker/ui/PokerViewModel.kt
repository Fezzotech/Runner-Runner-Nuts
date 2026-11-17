package com.example.offsuitpoker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.offsuitpoker.data.PlayerProfile
import com.example.offsuitpoker.data.ProfileManager
import com.example.offsuitpoker.model.ActionType
import com.example.offsuitpoker.model.TableState
import com.example.offsuitpoker.network.NetworkUtils
import com.example.offsuitpoker.network.PokerClient
import com.example.offsuitpoker.network.PokerHttpServer
import com.example.offsuitpoker.network.PokerServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URI

enum class Screen {
    LOBBY,
    TABLE,
    SETTINGS
}

class PokerViewModel(application: Application) : AndroidViewModel(application) {

    private val profileManager = ProfileManager(application)

    private val _profile = MutableStateFlow(profileManager.getProfile())
    val profile: StateFlow<PlayerProfile> = _profile.asStateFlow()

    private val _currentScreen = MutableStateFlow(Screen.LOBBY)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private var screenBeforeSettings: Screen = Screen.LOBBY

    fun openSettings() {
        if (_currentScreen.value != Screen.SETTINGS) {
            screenBeforeSettings = _currentScreen.value
        }
        _currentScreen.value = Screen.SETTINGS
    }

    fun closeSettings() {
        _currentScreen.value = screenBeforeSettings
    }

    private val _tableState = MutableStateFlow(TableState())
    val tableState: StateFlow<TableState> = _tableState.asStateFlow()

    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()

    private val _isPracticeMode = MutableStateFlow(false)
    val isPracticeMode: StateFlow<Boolean> = _isPracticeMode.asStateFlow()

    init {
        // Production: persistent lifetime stars and profile
        val p = profileManager.getProfile()
        com.example.offsuitpoker.util.CurrencyFormatter.initFromProfile(p)
        _profile.value = p
    }

    val localIpAddress: String = NetworkUtils.getLocalIpAddress()

    private var server: PokerServer? = null
    private var httpServer: PokerHttpServer? = null
    private var client: PokerClient? = null

    fun updateCurrency(symbol: String, useIndianFormat: Boolean) {
        profileManager.updateCurrency(symbol, useIndianFormat)
        val updated = profileManager.getProfile()
        com.example.offsuitpoker.util.CurrencyFormatter.initFromProfile(updated)
        _profile.value = updated
    }

    fun updatePlayerName(newName: String) {
        profileManager.updatePlayerName(newName)
        _profile.value = profileManager.getProfile()
    }

    fun buyInWithStar() {
        if (_isPracticeMode.value) {
            // In practice mode, refill practice chips to 10,000 without consuming lifetime stars!
            profileManager.refillPracticeChips()
            val updated = profileManager.getProfile()
            _profile.value = updated
            client?.sendRebuy(updated.playerId, 5, 10000)
        } else {
            val success = profileManager.buyInWithStar()
            if (success) {
                val updated = profileManager.getProfile()
                _profile.value = updated
                client?.sendRebuy(updated.playerId, updated.starsRemaining, 10000)
            }
        }
    }

    fun hostGame() {
        viewModelScope.launch(Dispatchers.IO) {
            _isPracticeMode.value = false
            val p = _profile.value
            // If player has 0 chips but stars remain, auto-buy in first star for LAN game
            if (p.currentChips <= 0 && p.starsRemaining > 0) {
                profileManager.buyInWithStar()
                _profile.value = profileManager.getProfile()
            }

            val currentProfile = _profile.value
            try {
                httpServer?.stop()
                server?.stop()
                httpServer = PokerHttpServer(8080, viewModelScope)
                httpServer?.start()
                server = PokerServer(8088, viewModelScope, currentProfile.playerId)
                server?.start()
                _isHost.value = true

                // Connect client locally with real LAN chips
                connectClient(
                    hostIp = "127.0.0.1",
                    profile = currentProfile,
                    chipsToJoin = currentProfile.currentChips,
                    onConnected = {
                        _currentScreen.value = Screen.TABLE
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun joinGame(hostIp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isPracticeMode.value = false
            val p = _profile.value
            if (p.currentChips <= 0 && p.starsRemaining > 0) {
                profileManager.buyInWithStar()
                _profile.value = profileManager.getProfile()
            }

            _isHost.value = false
            connectClient(
                hostIp = hostIp.trim(),
                profile = _profile.value,
                chipsToJoin = _profile.value.currentChips,
                onConnected = {
                    _currentScreen.value = Screen.TABLE
                }
            )
        }
    }

    fun startSoloPractice() {
        viewModelScope.launch(Dispatchers.IO) {
            _isPracticeMode.value = true
            var p = _profile.value
            // If practice chips are depleted, auto-refill to 10,000
            if (p.practiceChips <= 0) {
                profileManager.refillPracticeChips()
                _profile.value = profileManager.getProfile()
                p = _profile.value
            }

            try {
                httpServer?.stop()
                server?.stop()
                server = PokerServer(8088, viewModelScope, p.playerId)
                server?.start()
                _isHost.value = true

                connectClient(
                    hostIp = "127.0.0.1",
                    profile = p,
                    chipsToJoin = p.practiceChips,
                    onConnected = {
                        _currentScreen.value = Screen.TABLE
                    }
                )

                kotlinx.coroutines.delay(600)
                // Add 5 AI practice bots for a full 6-handed table matching reference design
                repeat(5) {
                    client?.addAiBot()
                    kotlinx.coroutines.delay(150)
                }
                kotlinx.coroutines.delay(300)
                client?.startHand()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun connectClient(hostIp: String, profile: PlayerProfile, chipsToJoin: Int, onConnected: () -> Unit) {
        try {
            client?.close()
            val cleanHost = hostIp.trim()
                .removePrefix("http://")
                .removePrefix("ws://")
                .substringBefore(":")
                .substringBefore("/")
            val uri = URI("ws://${cleanHost.ifEmpty { "127.0.0.1" }}:8088")
            client = PokerClient(
                serverUri = uri,
                onConnectedCallback = {
                    client?.joinTable(
                        playerId = profile.playerId,
                        playerName = profile.playerName,
                        stars = if (_isPracticeMode.value) 5 else profile.starsRemaining,
                        chips = chipsToJoin
                    )
                    viewModelScope.launch(Dispatchers.Main) {
                        onConnected()
                    }
                }
            )

            // Observe table states
            viewModelScope.launch {
                client?.tableState?.collect { state ->
                    _tableState.value = state

                    // Sync local chips with seat chips depending on mode (real LAN vs practice)
                    val mySeat = state.seats.firstOrNull { it.id == profile.playerId }
                    if (mySeat != null) {
                        if (_isPracticeMode.value) {
                            if (mySeat.chips != _profile.value.practiceChips) {
                                profileManager.updatePracticeChips(mySeat.chips)
                                _profile.value = profileManager.getProfile()
                            }
                        } else {
                            if (mySeat.chips != _profile.value.currentChips) {
                                profileManager.updateChips(mySeat.chips)
                                _profile.value = profileManager.getProfile()
                            }
                        }
                    }
                }
            }

            client?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendPlayerAction(action: ActionType, amount: Int = 0) {
        val pId = _profile.value.playerId
        client?.sendAction(pId, action, amount)
    }

    fun startHand() {
        client?.startHand()
    }

    fun addAiBot() {
        client?.addAiBot()
    }

    fun toggleFourColorDeck() {
        profileManager.toggleFourColorDeck()
        _profile.value = profileManager.getProfile()
    }

    fun toggleEscalatingBlinds() {
        client?.toggleBlinds()
    }

    fun rabbitHunt() {
        client?.rabbitHunt()
    }

    fun leaveTable() {
        _isPracticeMode.value = false
        client?.close()
        client = null
        if (_isHost.value) {
            try {
                httpServer?.stop()
                server?.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            httpServer = null
            server = null
        }
        _currentScreen.value = Screen.LOBBY
    }

    override fun onCleared() {
        super.onCleared()
        client?.close()
        try {
            httpServer?.stop()
            server?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        httpServer = null
        server = null
    }
}
