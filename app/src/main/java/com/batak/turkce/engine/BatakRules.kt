package com.batak.turkce.engine

import com.batak.turkce.model.PlayingCard
import com.batak.turkce.model.Rank
import com.batak.turkce.model.Suit
import kotlin.random.Random

object BatakRules {

    const val MIN_BID = 8
    const val MAX_BID = 13
    const val HAND_SIZE = 13
    const val PLAYER_COUNT = 4
    const val TRICK_SIZE = 4

    fun freshDeck(): List<PlayingCard> =
        Suit.entries.flatMap { suit -> Rank.entries.map { rank -> PlayingCard(suit, rank) } }

    fun shuffledDeck(random: Random): List<PlayingCard> = freshDeck().shuffled(random)

    fun deal(seed: Long, dealer: Int): List<List<PlayingCard>> {
        val deck = freshDeck().shuffled(Random(seed))
        val hands = List(PLAYER_COUNT) { mutableListOf<PlayingCard>() }
        deck.forEachIndexed { index, card ->
            hands[(dealer + 1 + index) % PLAYER_COUNT].add(card)
        }
        return hands.map { it.toList() }
    }

    fun legalMoves(hand: List<PlayingCard>, trick: List<PlayedCard>): List<PlayingCard> {
        if (trick.isEmpty()) return hand
        val ledSuit = trick.first().card.suit
        val follow = hand.filter { it.suit == ledSuit }
        return follow.ifEmpty { hand }
    }

    fun isValidMove(hand: List<PlayingCard>, trick: List<PlayedCard>, card: PlayingCard): Boolean =
        card in hand && card in legalMoves(hand, trick)

    fun trickWinner(trick: List<PlayedCard>, trump: Suit?): Int {
        require(trick.isNotEmpty()) { "Bos el icin kazanan hesaplanamaz" }
        val ledSuit = trick.first().card.suit
        var bestIndex = 0
        for (i in 1 until trick.size) {
            if (beats(trick[i].card, trick[bestIndex].card, ledSuit, trump)) bestIndex = i
        }
        return bestIndex
    }

    private fun beats(a: PlayingCard, b: PlayingCard, ledSuit: Suit, trump: Suit?): Boolean {
        val aTrump = trump != null && a.suit == trump
        val bTrump = trump != null && b.suit == trump
        return when {
            aTrump && bTrump -> a.rank.value > b.rank.value
            aTrump -> true
            bTrump -> false
            a.suit == ledSuit && b.suit == ledSuit -> a.rank.value > b.rank.value
            else -> false
        }
    }

    fun scoreRound(bids: List<Int>, tricksWon: List<Int>): List<Int> =
        bids.indices.map { i ->
            val bid = bids[i]
            when {
                bid <= 0 -> 0
                tricksWon[i] >= bid -> tricksWon[i]
                else -> -bid
            }
        }

    fun isValidBid(bid: Int, highestBid: Int): Boolean =
        bid == 0 || (bid in MIN_BID..MAX_BID && bid > highestBid)

    fun nextBidOptions(highestBid: Int): List<Int> {
        val min = maxOf(MIN_BID, highestBid + 1)
        return if (min > MAX_BID) emptyList() else (min..MAX_BID).toList()
    }

    fun trickOrder(startPlayer: Int): List<Int> = (0 until PLAYER_COUNT).map { (startPlayer + it) % PLAYER_COUNT }
}
