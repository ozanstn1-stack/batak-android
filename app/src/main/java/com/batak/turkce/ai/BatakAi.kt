package com.batak.turkce.ai

import com.batak.turkce.engine.BatakGame
import com.batak.turkce.engine.BatakRules
import com.batak.turkce.engine.PlayedCard
import com.batak.turkce.model.Difficulty
import com.batak.turkce.model.PlayingCard
import com.batak.turkce.model.Rank
import com.batak.turkce.model.Suit
import kotlin.random.Random

/**
 * Kural tabanli Batak yapay zekasi.
 * Uc zorluk seviyesi: Kolay, Normal, Zor.
 */
class BatakAi(
    val difficulty: Difficulty,
    private val random: Random = Random.Default
) {

    // ---------------------------------------------------------------- IHALE

    fun decideBid(hand: List<PlayingCard>, highestBid: Int, lastToBid: Boolean, passedCount: Int = 0): Int {
        val estimate = estimateTricks(hand)
        val minBid = maxOf(BatakRules.MIN_BID, highestBid + 1)
        if (minBid > BatakRules.MAX_BID) return 0

        val base = when (difficulty) {
            Difficulty.EASY -> 5.05 + random.nextDouble() * 0.7
            Difficulty.NORMAL -> 4.55 + random.nextDouble() * 0.4
            Difficulty.HARD -> 4.2 + random.nextDouble() * 0.35
        }
        val needed = base + (minBid - BatakRules.MIN_BID) * 1.0
        var wantBid = estimate >= needed

        if (!wantBid && highestBid == 0) {
            val readyToOpen = when (difficulty) {
                Difficulty.EASY -> passedCount >= 3
                Difficulty.NORMAL -> passedCount >= 2
                Difficulty.HARD -> passedCount >= 1
            }
            val rescue = when (difficulty) {
                Difficulty.EASY -> 3.4
                Difficulty.NORMAL -> 2.8
                Difficulty.HARD -> 2.35
            }
            if (readyToOpen && estimate >= rescue) wantBid = true
        }

        if (!wantBid && lastToBid && highestBid == 0 && estimate >= 2.05) wantBid = true

        if (difficulty == Difficulty.EASY && random.nextDouble() < 0.08) {
            wantBid = !wantBid
        }

        return if (wantBid) minBid else 0
    }

    fun estimateTricks(hand: List<PlayingCard>): Double {
        var total = 0.0
        val bySuit = hand.groupBy { it.suit }
        for (suit in Suit.entries) {
            val cards = bySuit[suit]?.sortedByDescending { it.rank.value } ?: continue
            val ranks = cards.map { it.rank }.toSet()
            for ((index, card) in cards.withIndex()) {
                val rank = card.rank
                val value = when (rank) {
                    Rank.ACE -> 1.0
                    Rank.KING -> if (Rank.ACE in ranks) 0.95 else 0.55
                    Rank.QUEEN -> when {
                        Rank.ACE in ranks && Rank.KING in ranks -> 0.9
                        Rank.ACE in ranks || Rank.KING in ranks -> 0.5
                        else -> 0.25
                    }
                    Rank.JACK -> {
                        val higher = listOf(Rank.ACE, Rank.KING, Rank.QUEEN).count { it in ranks }
                        when {
                            higher == 3 -> 0.7
                            higher == 2 -> 0.35
                            higher == 1 -> 0.12
                            else -> 0.0
                        }
                    }
                    else -> if (index == 0) 0.15 else 0.0
                }
                total += value
            }
            if (cards.size >= 5) total += (cards.size - 4) * 0.3
        }
        for (suit in Suit.entries) {
            when (bySuit[suit]?.size ?: 0) {
                0 -> total += 0.25
                1 -> total += 0.15
            }
        }
        return total
    }

    // ----------------------------------------------------------------- KOZ

    fun chooseTrump(hand: List<PlayingCard>): Suit {
        val bySuit = hand.groupBy { it.suit }
        var bestSuit = Suit.SPADES
        var bestScore = -1.0
        for (suit in Suit.entries) {
            val cards = bySuit[suit] ?: emptyList()
            var score = cards.size * 1.6
            for (card in cards) {
                score += when (card.rank) {
                    Rank.ACE -> 5.0
                    Rank.KING -> 3.2
                    Rank.QUEEN -> 2.0
                    Rank.JACK -> 1.2
                    Rank.TEN -> 0.6
                    else -> 0.0
                }
            }
            if (cards.size >= 5) score += (cards.size - 4) * 1.2
            if (score > bestScore) {
                bestScore = score
                bestSuit = suit
            }
        }
        return bestSuit
    }

    // ----------------------------------------------------------- KART OYNAMA

    fun chooseCard(game: BatakGame, player: Int): PlayingCard {
        val hand = game.hands[player]
        require(hand.isNotEmpty()) { "Bos elle kart secilemez" }
        val trump = game.trump ?: game.hands[player].firstOrNull()?.suit ?: Suit.SPADES
        val legal = BatakRules.legalMoves(hand, game.trick)
        if (legal.size == 1) return legal.first()
        return when (difficulty) {
            Difficulty.EASY -> easyCard(hand, legal, game.trick, trump, player)
            Difficulty.NORMAL -> normalCard(hand, legal, game, trump, player)
            Difficulty.HARD -> hardCard(hand, legal, game, trump, player)
        }
    }

    private fun winningCards(
        legal: List<PlayingCard>,
        trick: List<PlayedCard>,
        trump: Suit,
        me: Int
    ): List<PlayingCard> {
        if (trick.isEmpty()) return emptyList()
        return legal.filter { card ->
            val simulated = trick + PlayedCard(me, card)
            simulated[BatakRules.trickWinner(simulated, trump)].player == me
        }
    }

    private fun cost(card: PlayingCard, trump: Suit): Int =
        card.rank.value + if (card.suit == trump) 20 else 0

    private fun easyCard(
        hand: List<PlayingCard>,
        legal: List<PlayingCard>,
        trick: List<PlayedCard>,
        trump: Suit,
        me: Int
    ): PlayingCard {
        if (trick.isEmpty()) {
            val sorted = legal.sortedBy { it.rank.value }
            return if (random.nextDouble() < 0.6 && sorted.size > 1) {
                sorted[random.nextInt((sorted.size + 1) / 2)]
            } else {
                sorted[random.nextInt(sorted.size)]
            }
        }
        val winners = winningCards(legal, trick, trump, me)
        return when {
            winners.isNotEmpty() && random.nextDouble() < 0.5 -> winners[random.nextInt(winners.size)]
            else -> legal[random.nextInt(legal.size)]
        }
    }

    private fun normalCard(
        hand: List<PlayingCard>,
        legal: List<PlayingCard>,
        game: BatakGame,
        trump: Suit,
        me: Int
    ): PlayingCard {
        val trick = game.trick
        if (trick.isEmpty()) return normalLead(hand, legal, trump)

        val winners = winningCards(legal, trick, trump, me)
        if (winners.isNotEmpty()) return winners.minBy { cost(it, trump) }

        val nonTrump = legal.filter { it.suit != trump }
        val pool = nonTrump.ifEmpty { legal }
        return pool.minBy { it.rank.value }
    }

    private fun normalLead(hand: List<PlayingCard>, legal: List<PlayingCard>, trump: Suit): PlayingCard {
        val myTrumps = legal.filter { it.suit == trump }
        if (myTrumps.size >= 4 && random.nextDouble() < 0.55) {
            return myTrumps.maxBy { it.rank.value }
        }
        val aces = legal.filter { it.rank == Rank.ACE }
        if (aces.isNotEmpty() && random.nextDouble() < 0.75) {
            return aces.minBy { suitLength(hand, it.suit) }
        }
        val nonTrump = legal.filter { it.suit != trump }
        val pool = nonTrump.ifEmpty { legal }
        val bySuit = pool.groupBy { it.suit }
        val longest = bySuit.maxBy { it.value.size }.value
        return longest.minBy { it.rank.value }
    }

    private fun hardCard(
        hand: List<PlayingCard>,
        legal: List<PlayingCard>,
        game: BatakGame,
        trump: Suit,
        me: Int
    ): PlayingCard {
        val played = game.playedCards.toSet()
        val voids = knownVoids(game, me)
        val trick = game.trick
        if (trick.isEmpty()) return hardLead(hand, legal, trump, played, voids, me)

        val winners = winningCards(legal, trick, trump, me)
        if (winners.isNotEmpty()) {
            val nonTrumpWinners = winners.filter { it.suit != trump }
            val pool = nonTrumpWinners.ifEmpty { winners }
            return pool.minBy { it.rank.value }
        }

        val ledSuit = trick.first().card.suit
        val safeDiscards = legal.filter { it.suit != trump && it.suit != ledSuit }
        val nonTrump = legal.filter { it.suit != trump }
        val pool = safeDiscards.ifEmpty { nonTrump }.ifEmpty { legal }
        val bySuit = pool.groupBy { it.suit }
        val shortest = bySuit.minBy { it.value.size }.value
        return shortest.minBy { it.rank.value }
    }

    private fun hardLead(
        hand: List<PlayingCard>,
        legal: List<PlayingCard>,
        trump: Suit,
        played: Set<PlayingCard>,
        voids: Map<Int, Set<Suit>>,
        me: Int
    ): PlayingCard {
        val opponents = (0..3).filter { it != me }
        fun voidCount(suit: Suit) = opponents.count { suit in (voids[it] ?: emptySet()) }

        val bossCards = legal.filter { isBoss(it, hand, played) }
        val trumpBosses = bossCards.filter { it.suit == trump }
        val myTrumpCount = hand.count { it.suit == trump }

        if (trumpBosses.isNotEmpty() && myTrumpCount >= 3) {
            return trumpBosses.maxBy { it.rank.value }
        }

        val solidSideBosses = bossCards.filter { it.suit != trump && voidCount(it.suit) == 0 }
        if (solidSideBosses.isNotEmpty()) {
            return solidSideBosses.minBy { suitLength(hand, it.suit) }
        }

        if (myTrumpCount >= 4 && random.nextDouble() < 0.5) {
            val highTrumps = legal.filter { it.suit == trump && it.rank.value >= Rank.KING.value }
            if (highTrumps.isNotEmpty()) return highTrumps.minBy { it.rank.value }
        }

        val safe = legal.filter { it.suit != trump && voidCount(it.suit) == 0 }
        val nonTrump = legal.filter { it.suit != trump }
        val pool = safe.ifEmpty { nonTrump }.ifEmpty { legal }
        val bySuit = pool.groupBy { it.suit }
        val longest = bySuit.maxBy { it.value.size }.value
        return longest.minBy { it.rank.value }
    }

    private fun knownVoids(game: BatakGame, me: Int): Map<Int, Set<Suit>> {
        val voids = mutableMapOf<Int, MutableSet<Suit>>()
        for (trick in game.completedTricks) {
            if (trick.isEmpty()) continue
            val ledSuit = trick.first().card.suit
            for (play in trick) {
                if (play.player == me) continue
                if (play.card.suit != ledSuit && play.card.suit != game.trump) {
                    voids.getOrPut(play.player) { mutableSetOf() }.add(ledSuit)
                }
            }
        }
        return voids
    }

    private fun isBoss(card: PlayingCard, hand: List<PlayingCard>, played: Set<PlayingCard>): Boolean {
        val min = card.rank.value + 1
        if (min > Rank.ACE.value) return true
        for (value in min..Rank.ACE.value) {
            val higher = PlayingCard(card.suit, Rank.byValue.getValue(value))
            if (higher in hand) continue
            if (higher in played) continue
            return false
        }
        return true
    }

    private fun suitLength(hand: List<PlayingCard>, suit: Suit): Int = hand.count { it.suit == suit }
}
