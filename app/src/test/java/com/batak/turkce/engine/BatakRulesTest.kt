package com.batak.turkce.engine

import com.batak.turkce.model.PlayingCard
import com.batak.turkce.model.Rank
import com.batak.turkce.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatakRulesTest {

    @Test
    fun `deste 52 benzersiz kart icerir`() {
        val deck = BatakRules.freshDeck()
        assertEquals(52, deck.size)
        assertEquals(52, deck.toSet().size)
        assertEquals(4, deck.map { it.suit }.toSet().size)
    }

    @Test
    fun `kart siralamasi dogru`() {
        assertTrue(Rank.ACE.value > Rank.KING.value)
        assertTrue(Rank.KING.value > Rank.QUEEN.value)
        assertTrue(Rank.QUEEN.value > Rank.JACK.value)
        assertTrue(Rank.JACK.value > Rank.TEN.value)
        assertTrue(Rank.TEN.value > Rank.NINE.value)
        assertTrue(Rank.TWO.value < Rank.THREE.value)
    }

    @Test
    fun `dagatim her oyuncuya 13 kart verir`() {
        for (seed in 1L..40L) {
            val hands = BatakRules.deal(seed, (seed % 4).toInt())
            assertEquals(4, hands.size)
            hands.forEach { assertEquals(13, it.size) }
            val all = hands.flatten()
            assertEquals(52, all.size)
            assertEquals(52, all.toSet().size)
        }
    }

    @Test
    fun `dagatim sirali ve tutarli`() {
        val first = BatakRules.deal(7L, 1)
        val second = BatakRules.deal(7L, 1)
        assertEquals(first, second)
    }

    @Test
    fun `takim rengi zorunlulugu uygulanir`() {
        val hand = listOf(
            card(Suit.SPADES, Rank.ACE),
            card(Suit.SPADES, Rank.FIVE),
            card(Suit.HEARTS, Rank.KING),
            card(Suit.CLUBS, Rank.TWO)
        )
        val trick = listOf(PlayedCard(1, card(Suit.SPADES, Rank.NINE)))
        val legal = BatakRules.legalMoves(hand, trick)
        assertEquals(2, legal.size)
        assertTrue(legal.all { it.suit == Suit.SPADES })
        assertFalse(BatakRules.isValidMove(hand, trick, card(Suit.HEARTS, Rank.KING)))
    }

    @Test
    fun `renk yoksa her kart oynanabilir`() {
        val hand = listOf(
            card(Suit.HEARTS, Rank.KING),
            card(Suit.CLUBS, Rank.TWO)
        )
        val trick = listOf(PlayedCard(1, card(Suit.SPADES, Rank.NINE)))
        val legal = BatakRules.legalMoves(hand, trick)
        assertEquals(2, legal.size)
        assertTrue(BatakRules.isValidMove(hand, trick, card(Suit.CLUBS, Rank.TWO)))
    }

    @Test
    fun `bos el icin tum kartlar oynanabilir`() {
        val hand = BatakRules.deal(3L, 0)[0].toList()
        assertEquals(13, BatakRules.legalMoves(hand, emptyList()).size)
    }

    @Test
    fun `koz yoksa en yuksek takim karti eli kazanir`() {
        val trick = listOf(
            PlayedCard(0, card(Suit.CLUBS, Rank.TEN)),
            PlayedCard(1, card(Suit.CLUBS, Rank.ACE)),
            PlayedCard(2, card(Suit.HEARTS, Rank.KING)),
            PlayedCard(3, card(Suit.CLUBS, Rank.SIX))
        )
        assertEquals(1, BatakRules.trickWinner(trick, null))
    }

    @Test
    fun `koz eli kazanir`() {
        val trick = listOf(
            PlayedCard(0, card(Suit.CLUBS, Rank.ACE)),
            PlayedCard(1, card(Suit.HEARTS, Rank.TWO)),
            PlayedCard(2, card(Suit.CLUBS, Rank.KING)),
            PlayedCard(3, card(Suit.CLUBS, Rank.FIVE))
        )
        assertEquals(1, BatakRules.trickWinner(trick, Suit.HEARTS))
    }

    @Test
    fun `en yuksek koz digerlerini yener`() {
        val trick = listOf(
            PlayedCard(0, card(Suit.CLUBS, Rank.ACE)),
            PlayedCard(1, card(Suit.HEARTS, Rank.TWO)),
            PlayedCard(2, card(Suit.HEARTS, Rank.TEN)),
            PlayedCard(3, card(Suit.CLUBS, Rank.FIVE))
        )
        assertEquals(2, BatakRules.trickWinner(trick, Suit.HEARTS))
    }

    @Test
    fun `farkli renkten atilan kart eli kazanamaz`() {
        val trick = listOf(
            PlayedCard(0, card(Suit.CLUBS, Rank.TWO)),
            PlayedCard(1, card(Suit.DIAMONDS, Rank.ACE)),
            PlayedCard(2, card(Suit.HEARTS, Rank.ACE)),
            PlayedCard(3, card(Suit.CLUBS, Rank.THREE))
        )
        assertEquals(3, BatakRules.trickWinner(trick, null))
    }

    @Test
    fun `puanlama ornek durumlari dogru hesaplar`() {
        val bids = listOf(9, 8, 0, 12)
        val tricks = listOf(10, 7, 2, 1)
        val scores = BatakRules.scoreRound(bids, tricks)
        assertEquals(10, scores[0])
        assertEquals(-8, scores[1])
        assertEquals(0, scores[2])
        assertEquals(-12, scores[3])
    }

    @Test
    fun `puanlama tam basarili ihalede el sayisini verir`() {
        val scores = BatakRules.scoreRound(listOf(8, 0, 13, 0), listOf(13, 0, 13, 0))
        assertEquals(13, scores[0])
        assertEquals(0, scores[1])
        assertEquals(13, scores[2])
        assertEquals(0, scores[3])
    }

    @Test
    fun `ihale dogrulama kurallari`() {
        assertTrue(BatakRules.isValidBid(0, 0))
        assertTrue(BatakRules.isValidBid(8, 0))
        assertTrue(BatakRules.isValidBid(9, 8))
        assertFalse(BatakRules.isValidBid(8, 8))
        assertFalse(BatakRules.isValidBid(7, 0))
        assertFalse(BatakRules.isValidBid(14, 0))
        assertFalse(BatakRules.isValidBid(13, 13))
    }

    @Test
    fun `gecerli teklif listesi dogru`() {
        assertEquals(listOf(8, 9, 10, 11, 12, 13), BatakRules.nextBidOptions(0))
        assertEquals(listOf(10, 11, 12, 13), BatakRules.nextBidOptions(9))
        assertEquals(emptyList<Int>(), BatakRules.nextBidOptions(13))
    }

    private fun card(suit: Suit, rank: Rank): PlayingCard = PlayingCard(suit, rank)
}
