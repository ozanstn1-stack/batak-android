package com.batak.turkce.engine

import com.batak.turkce.model.Difficulty
import com.batak.turkce.model.PlayingCard
import com.batak.turkce.model.Suit
import kotlinx.serialization.Serializable

@Serializable
enum class GamePhase {
    DEALING,
    BIDDING,
    TRUMP_SELECTION,
    PLAYING,
    TRICK_RESOLUTION,
    ROUND_OVER,
    GAME_OVER
}

@Serializable
data class PlayedCard(val player: Int, val card: PlayingCard)

@Serializable
data class RoundResult(
    val bids: List<Int>,
    val tricksWon: List<Int>,
    val scores: List<Int>,
    val trump: Suit
)

@Serializable
data class BatakGame(
    val playerName: String = "Sen",
    val difficulty: Difficulty = Difficulty.NORMAL,
    val totalRounds: Int = 5,
    val roundNumber: Int = 1,
    val dealer: Int = 3,
    val scores: List<Int> = List(4) { 0 },
    val cumulativeTricks: List<Int> = List(4) { 0 },
    val statsApplied: Boolean = false,
    val hands: List<List<PlayingCard>> = emptyList(),
    val phase: GamePhase = GamePhase.DEALING,
    val dealId: Int = 0,
    val bids: List<Int?> = List(4) { null },
    val bidTurn: Int = 0,
    val highestBid: Int = 0,
    val highestBidder: Int = -1,
    val trump: Suit? = null,
    val currentPlayer: Int = 0,
    val trick: List<PlayedCard> = emptyList(),
    val tricksWon: List<Int> = List(4) { 0 },
    val completedTricks: List<List<PlayedCard>> = emptyList(),
    val playedCards: List<PlayingCard> = emptyList(),
    val lastTrickWinner: Int = -1,
    val roundResult: RoundResult? = null,
    val redealCount: Int = 0
) {
    val tricksPlayed: Int get() = completedTricks.size + if (phase == GamePhase.TRICK_RESOLUTION) 1 else 0
    val humanHand: List<PlayingCard> get() = hands.getOrElse(0) { emptyList() }
    val isHumanTurn: Boolean get() = currentPlayer == 0
    val isEmptyTrick: Boolean get() = trick.isEmpty()
}
