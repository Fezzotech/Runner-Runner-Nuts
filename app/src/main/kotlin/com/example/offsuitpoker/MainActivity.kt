package com.example.offsuitpoker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.offsuitpoker.ui.PokerViewModel
import com.example.offsuitpoker.ui.Screen
import com.example.offsuitpoker.ui.screens.LobbyScreen
import com.example.offsuitpoker.ui.screens.PokerTableScreen
import com.example.offsuitpoker.ui.screens.SettingsScreen
import com.example.offsuitpoker.ui.theme.OffsuitPokerTheme
import com.example.offsuitpoker.util.AppLogger

class MainActivity : ComponentActivity() {

    private val viewModel: PokerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.init(applicationContext)

        // Force true dark system bars with white status bar icons (time, battery, Wi-Fi)
        val darkBg = android.graphics.Color.parseColor("#08080A")
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(darkBg),
            navigationBarStyle = SystemBarStyle.dark(darkBg)
        )

        window.statusBarColor = darkBg
        window.navigationBarColor = darkBg
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContent {
            OffsuitPokerTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF08080A))
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val profile by viewModel.profile.collectAsState()
                    val tableState by viewModel.tableState.collectAsState()
                    val isHost by viewModel.isHost.collectAsState()
                    val isPracticeMode by viewModel.isPracticeMode.collectAsState()

                    when (currentScreen) {
                        Screen.LOBBY -> {
                            LobbyScreen(
                                profile = profile,
                                localIpAddress = viewModel.localIpAddress,
                                onUpdateName = { viewModel.updatePlayerName(it) },
                                onBuyInStar = { viewModel.buyInWithStar() },
                                onHostGame = { viewModel.hostGame() },
                                onJoinGame = { hostIp -> viewModel.joinGame(hostIp) },
                                onSoloPractice = { viewModel.startSoloPractice() },
                                onOpenSettings = { viewModel.openSettings() }
                            )
                        }

                        Screen.TABLE -> {
                            BackHandler {
                                viewModel.leaveTable()
                            }

                            PokerTableScreen(
                                profile = profile,
                                tableState = tableState,
                                isHost = isHost,
                                isPracticeMode = isPracticeMode,
                                hostIp = viewModel.localIpAddress,
                                onAction = { action, amount -> viewModel.sendPlayerAction(action, amount) },
                                onStartHand = { viewModel.startHand() },
                                onAddAiBot = { viewModel.addAiBot() },
                                onToggleFourColor = { viewModel.toggleFourColorDeck() },
                                onToggleEscalatingBlinds = { viewModel.toggleEscalatingBlinds() },
                                onRabbitHunt = { viewModel.rabbitHunt() },
                                onRebuyStar = { viewModel.buyInWithStar() },
                                onLeaveTable = { viewModel.leaveTable() },
                                onOpenSettings = { viewModel.openSettings() }
                            )
                        }

                        Screen.SETTINGS -> {
                            BackHandler {
                                viewModel.closeSettings()
                            }

                            SettingsScreen(
                                profile = profile,
                                tableState = tableState,
                                localIpAddress = viewModel.localIpAddress,
                                onToggleFourColor = { viewModel.toggleFourColorDeck() },
                                onUpdateCurrency = { symbol, isIndian -> viewModel.updateCurrency(symbol, isIndian) },
                                onBack = { viewModel.closeSettings() }
                            )
                        }
                    }
                }
            }
        }
    }
}
