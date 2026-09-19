package com.batak.turkce.engine

import com.batak.turkce.model.GameMode
import com.batak.turkce.model.PlayingCard
import com.batak.turkce.model.Rank
import com.batak.turkce.model.Suit
import kotlin.random.Random

object BatakRules {

    const val MIN_BID_SOLO = 5
    const val MIN_BID_PARTNERED = 7
    const val MAX_BID = 13
    const val HAND_SIZE = 13
    const val PLAYER_COUNT = 4
    const val TRICK_SIZE = 4

    fun minBid(mode: GameMode): Int = when (mode) {
        GameMode.SOLO -> MIN_BID_SOLO
        GameMode.PARTNERED -> MIN_BID_PARTNERED
    }

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

    /**
     * Takim rengi zorunlulugu ve kart yukseltme zorunlulugu:
     * - Elinde takim rengi varsa o renkten oynamak zorundadir.
     * - Oynanan en yuksek takim rengi kartindan buyuk bir karti varsa, onu oynamak zorundadir.
     * - Rengi yoksa istedigi karti oynayabilir (koz atma zorunlulugu yoktur).
     */
    fun legalMoves(hand: List<PlayingCard>, trick: List<PlayedCard>): List<PlayingCard> {
        if (trick.isEmpty()) return hand
        val ledSuit = trick.first().card.suit
        val follow = hand.filter { it.suit == ledSuit }
        if (follow.isEmpty()) return hand
        val highestLed = trick.filter { it.card.suit == ledSuit }.maxOf { it.card.rank.value }
        val higher = follow.filter { it.rank.value > highestLed }
        return higher.ifEmpty { follow }
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

    fun isPartner(a: Int, b: Int): Boolean = a % 2 == b % 2

    fun teamOf(player: Int): Int = player % 2

    /**
     * Eşsiz (tek) modda her oyuncu kendi ihalesini tutmaya çalışır:
     * tutan aldığı el kadar artı puan, tutamayan ihalesi kadar eksi puan, pas geçen 0.
     *
     * Eşli modda yalnızca en yüksek ihaleyi veren takım sözleşmelidir:
     * takım toplam eli ihaleyi tuttuğunda iki üyeye de + ihale, tutamadığında - ihale,
     * rakip takım üyeleri ise ihale kadar puan alır.
     */
    fun scoreRound(
        bids: List<Int>,
        tricksWon: List<Int>,
        mode: GameMode,
        highestBidder: Int = -1,
        highestBid: Int = 0
    ): List<Int> {
        return when (mode) {
            GameMode.SOLO -> bids.indices.map { i ->
                val bid = bids[i]
                when {
                    bid <= 0 -> 0
                    tricksWon[i] >= bid -> tricksWon[i]
                    else -> -bid
                }
            }
            GameMode.PARTNERED -> {
                val contractTeam = teamOf(highestBidder.coerceAtLeast(0))
                val bid = if (highestBid > 0) highestBid else bids.maxOrNull() ?: 0
                val teamTricks = tricksWon.indices.filter { teamOf(it) == contractTeam }.sumOf { tricksWon[it] }
                val made = teamTricks >= bid
                List(PLAYER_COUNT) { i ->
                    val inContract = teamOf(i) == contractTeam
                    when {
                        inContract && made -> bid
                        inContract -> -bid
                        !made && bid > 0 -> bid
                        else -> 0
                    }
                }
            }
        }
    }

    fun isValidBid(bid: Int, highestBid: Int, mode: GameMode): Boolean =
        bid == 0 || (bid in minBid(mode)..MAX_BID && bid > highestBid)

    fun nextBidOptions(highestBid: Int, mode: GameMode): List<Int> {
        val min = maxOf(minBid(mode), highestBid + 1)
        return if (min > MAX_BID) emptyList() else (min..MAX_BID).toList()
    }

    fun trickOrder(startPlayer: Int): List<Int> = (0 until PLAYER_COUNT).map { (startPlayer + it) % PLAYER_COUNT }
}
