package com.batak.turkce.data

import com.batak.turkce.model.AppTheme
import com.batak.turkce.model.CardDesign
import com.batak.turkce.model.Difficulty
import com.batak.turkce.model.GameMode

data class AppSettings(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val gameMode: GameMode = GameMode.SOLO,
    val cardDesign: CardDesign = CardDesign.KLASIK,
    val theme: AppTheme = AppTheme.DARK,
    val playerName: String = "Oyuncu",
    val roundsPerGame: Int = 5
)

data class UserStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val totalTricks: Int = 0,
    val highestScore: Int = 0,
    val longestWinStreak: Int = 0,
    val currentWinStreak: Int = 0
) {
    val winRate: Float
        get() = if (gamesPlayed == 0) 0f else gamesWon.toFloat() / gamesPlayed.toFloat()
}
