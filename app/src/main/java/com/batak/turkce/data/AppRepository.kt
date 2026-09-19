package com.batak.turkce.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.batak.turkce.engine.BatakGame
import com.batak.turkce.model.AppTheme
import com.batak.turkce.model.CardDesign
import com.batak.turkce.model.Difficulty
import com.batak.turkce.model.GameMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "batak_prefs")

class AppRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private object Keys {
        val SOUND = booleanPreferencesKey("sound_enabled")
        val VIBRATION = booleanPreferencesKey("vibration_enabled")
        val ANIMATIONS = booleanPreferencesKey("animations_enabled")
        val DIFFICULTY = stringPreferencesKey("difficulty")
        val GAME_MODE = stringPreferencesKey("game_mode")
        val CARD_DESIGN = stringPreferencesKey("card_design")
        val THEME = stringPreferencesKey("theme")
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val ROUNDS = intPreferencesKey("rounds_per_game")

        val GAMES_PLAYED = intPreferencesKey("games_played")
        val GAMES_WON = intPreferencesKey("games_won")
        val GAMES_LOST = intPreferencesKey("games_lost")
        val TOTAL_TRICKS = intPreferencesKey("total_tricks")
        val HIGHEST_SCORE = intPreferencesKey("highest_score")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val CURRENT_STREAK = intPreferencesKey("current_streak")

        val SAVED_GAME = stringPreferencesKey("saved_game")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            soundEnabled = prefs[Keys.SOUND] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
            animationsEnabled = prefs[Keys.ANIMATIONS] ?: true,
            difficulty = prefs[Keys.DIFFICULTY]?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() } ?: Difficulty.NORMAL,
            gameMode = prefs[Keys.GAME_MODE]?.let { runCatching { GameMode.valueOf(it) }.getOrNull() } ?: GameMode.SOLO,
            cardDesign = prefs[Keys.CARD_DESIGN]?.let { runCatching { CardDesign.valueOf(it) }.getOrNull() } ?: CardDesign.KLASIK,
            theme = prefs[Keys.THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.DARK,
            playerName = prefs[Keys.PLAYER_NAME] ?: "Oyuncu",
            roundsPerGame = (prefs[Keys.ROUNDS] ?: 5).coerceIn(1, 20)
        )
    }

    val statsFlow: Flow<UserStats> = context.dataStore.data.map { prefs ->
        UserStats(
            gamesPlayed = prefs[Keys.GAMES_PLAYED] ?: 0,
            gamesWon = prefs[Keys.GAMES_WON] ?: 0,
            gamesLost = prefs[Keys.GAMES_LOST] ?: 0,
            totalTricks = prefs[Keys.TOTAL_TRICKS] ?: 0,
            highestScore = prefs[Keys.HIGHEST_SCORE] ?: 0,
            longestWinStreak = prefs[Keys.LONGEST_STREAK] ?: 0,
            currentWinStreak = prefs[Keys.CURRENT_STREAK] ?: 0
        )
    }

    val saveFlow: Flow<BatakGame?> = context.dataStore.data.map { prefs ->
        prefs[Keys.SAVED_GAME]?.let { raw ->
            runCatching { json.decodeFromString<BatakGame>(raw) }.getOrNull()
        }
    }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { prefs ->
            val current = AppSettings(
                soundEnabled = prefs[Keys.SOUND] ?: true,
                vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
                animationsEnabled = prefs[Keys.ANIMATIONS] ?: true,
                difficulty = prefs[Keys.DIFFICULTY]?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() } ?: Difficulty.NORMAL,
                gameMode = prefs[Keys.GAME_MODE]?.let { runCatching { GameMode.valueOf(it) }.getOrNull() } ?: GameMode.SOLO,
                cardDesign = prefs[Keys.CARD_DESIGN]?.let { runCatching { CardDesign.valueOf(it) }.getOrNull() } ?: CardDesign.KLASIK,
                theme = prefs[Keys.THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.DARK,
                playerName = prefs[Keys.PLAYER_NAME] ?: "Oyuncu",
                roundsPerGame = (prefs[Keys.ROUNDS] ?: 5).coerceIn(1, 20)
            )
            val updated = transform(current)
            prefs[Keys.SOUND] = updated.soundEnabled
            prefs[Keys.VIBRATION] = updated.vibrationEnabled
            prefs[Keys.ANIMATIONS] = updated.animationsEnabled
            prefs[Keys.DIFFICULTY] = updated.difficulty.name
            prefs[Keys.GAME_MODE] = updated.gameMode.name
            prefs[Keys.CARD_DESIGN] = updated.cardDesign.name
            prefs[Keys.THEME] = updated.theme.name
            prefs[Keys.PLAYER_NAME] = updated.playerName.take(16)
            prefs[Keys.ROUNDS] = updated.roundsPerGame.coerceIn(1, 20)
        }
    }

    suspend fun recordMatchFinished(humanScore: Int, won: Boolean, humanTricks: Int) {
        context.dataStore.edit { prefs ->
            val played = (prefs[Keys.GAMES_PLAYED] ?: 0) + 1
            val wins = (prefs[Keys.GAMES_WON] ?: 0) + if (won) 1 else 0
            val losses = (prefs[Keys.GAMES_LOST] ?: 0) + if (won) 0 else 1
            val streak = if (won) (prefs[Keys.CURRENT_STREAK] ?: 0) + 1 else 0
            prefs[Keys.GAMES_PLAYED] = played
            prefs[Keys.GAMES_WON] = wins
            prefs[Keys.GAMES_LOST] = losses
            prefs[Keys.CURRENT_STREAK] = streak
            prefs[Keys.LONGEST_STREAK] = maxOf(prefs[Keys.LONGEST_STREAK] ?: 0, streak)
            prefs[Keys.TOTAL_TRICKS] = (prefs[Keys.TOTAL_TRICKS] ?: 0) + humanTricks
            prefs[Keys.HIGHEST_SCORE] = maxOf(prefs[Keys.HIGHEST_SCORE] ?: 0, humanScore)
        }
    }

    suspend fun resetStats() {
        context.dataStore.edit { prefs ->
            prefs[Keys.GAMES_PLAYED] = 0
            prefs[Keys.GAMES_WON] = 0
            prefs[Keys.GAMES_LOST] = 0
            prefs[Keys.TOTAL_TRICKS] = 0
            prefs[Keys.HIGHEST_SCORE] = 0
            prefs[Keys.LONGEST_STREAK] = 0
            prefs[Keys.CURRENT_STREAK] = 0
        }
    }

    suspend fun saveGame(game: BatakGame) {
        val raw = runCatching { json.encodeToString(game) }.getOrNull() ?: return
        context.dataStore.edit { prefs -> prefs[Keys.SAVED_GAME] = raw }
    }

    suspend fun clearSave() {
        context.dataStore.edit { prefs -> prefs.remove(Keys.SAVED_GAME) }
    }

    suspend fun loadSavedGame(): BatakGame? {
        var result: BatakGame? = null
        context.dataStore.edit { prefs ->
            result = prefs[Keys.SAVED_GAME]?.let { raw ->
                runCatching { json.decodeFromString<BatakGame>(raw) }.getOrNull()
            }
        }
        return result
    }
}
