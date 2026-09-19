package com.batak.turkce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batak.turkce.game.GameViewModel
import com.batak.turkce.ui.screens.AboutScreen
import com.batak.turkce.ui.screens.GameScreen
import com.batak.turkce.ui.screens.HowToPlayScreen
import com.batak.turkce.ui.screens.MainMenuScreen
import com.batak.turkce.ui.screens.SettingsScreen
import com.batak.turkce.ui.screens.StatsScreen
import com.batak.turkce.ui.theme.BatakTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GameViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            BatakTheme(theme = settings.theme) {
                BatakRoot(viewModel)
            }
        }
    }
}

private enum class Screen { MENU, GAME, SETTINGS, HOW_TO, STATS, ABOUT }

@Composable
private fun BatakRoot(vm: GameViewModel) {
    var screen by rememberSaveable { mutableStateOf(Screen.MENU.name) }
    val hasSave by vm.hasSave.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()

    BackHandler(enabled = screen != Screen.MENU.name) {
        if (screen == Screen.GAME.name) vm.backToMenuKeepGame()
        screen = Screen.MENU.name
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        when (screen) {
            Screen.MENU.name -> MainMenuScreen(
                hasSave = hasSave,
                playerName = settings.playerName,
                cardDesign = settings.cardDesign,
                onContinue = {
                    vm.playClick()
                    vm.resumeSavedGame()
                    screen = Screen.GAME.name
                },
                onNewGame = {
                    vm.playClick()
                    vm.startNewMatch()
                    screen = Screen.GAME.name
                },
                onSettings = {
                    vm.playClick()
                    screen = Screen.SETTINGS.name
                },
                onHowTo = {
                    vm.playClick()
                    screen = Screen.HOW_TO.name
                },
                onStats = {
                    vm.playClick()
                    screen = Screen.STATS.name
                },
                onAbout = {
                    vm.playClick()
                    screen = Screen.ABOUT.name
                }
            )

            Screen.GAME.name -> GameScreen(
                vm = vm,
                onExit = { screen = Screen.MENU.name }
            )

            Screen.SETTINGS.name -> SettingsScreen(
                vm = vm,
                onBack = { screen = Screen.MENU.name }
            )

            Screen.HOW_TO.name -> HowToPlayScreen(
                vm = vm,
                onBack = { screen = Screen.MENU.name }
            )

            Screen.STATS.name -> StatsScreen(
                vm = vm,
                onBack = { screen = Screen.MENU.name }
            )

            Screen.ABOUT.name -> AboutScreen(
                vm = vm,
                onBack = { screen = Screen.MENU.name }
            )
        }
    }
}
