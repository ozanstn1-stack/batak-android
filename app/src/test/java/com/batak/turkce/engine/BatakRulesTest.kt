package com.batak.turkce.engine

import com.batak.turkce.model.GameMode
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
        val legal = BatakRules.legalMoves(hand, trick, Suit.HEARTS)
        assertEquals(listOf(card(Suit.SPADES, Rank.ACE)), legal)
        assertFalse(BatakRules.isValidMove(hand, trick, card(Suit.HEARTS, Rank.KING), Suit.HEARTS))
        assertFalse(BatakRules.isValidMove(hand, trick, card(Suit.SPADES, Rank.FIVE), Suit.HEARTS))
        assertTrue(BatakRules.isValidMove(hand, trick, card(Suit.SPADES, Rank.ACE), Suit.HEARTS))
    }

    @Test
    fun `kart yukseltme zorunlulugu uygulanir`() {
        val hand = listOf(
            card(Suit.HEARTS, Rank.TWO),
            card(Suit.HEARTS, Rank.KING),
            card(Suit.HEARTS, Rank.SIX),
            card(Suit.CLUBS, Rank.THREE)
        )
        val trick = listOf(
            PlayedCard(0, card(Suit.HEARTS, Rank.NINE)),
            PlayedCard(1, card(Suit.HEARTS, Rank.FOUR))
        )
        val legal = BatakRules.legalMoves(hand, trick, Suit.CLUBS)
        assertEquals(listOf(card(Suit.HEARTS, Rank.KING)), legal)
    }

    @Test
    fun `yukseltme yapamiyorsa rengi takip etmek yeterli`() {
        val hand = listOf(
            card(Suit.HEARTS, Rank.TWO),
            card(Suit.HEARTS, Rank.THREE),
            card(Suit.CLUBS, Rank.ACE)
        )
        val trick = listOf(
            PlayedCard(0, card(Suit.HEARTS, Rank.KING)),
            PlayedCard(1, card(Suit.HEARTS, Rank.SIX))
        )
        val legal = BatakRules.legalMoves(hand, trick, Suit.CLUBS)
        assertEquals(2, legal.size)
        assertTrue(legal.all { it.suit == Suit.HEARTS })
    }

    @Test
    fun `renk yoksa koz disi kartlar oynanabilir`() {
        val hand = listOf(
            card(Suit.HEARTS, Rank.KING),
            card(Suit.CLUBS, Rank.TWO)
        )
        val trick = listOf(PlayedCard(1, card(Suit.SPADES, Rank.NINE)))
        val legal = BatakRules.legalMoves(hand, trick, Suit.DIAMONDS)
        assertEquals(2, legal.size)
        assertTrue(BatakRules.isValidMove(hand, trick, card(Suit.CLUBS, Rank.TWO), Suit.DIAMONDS))
    }

    @Test
    fun `ele kozla baslanamaz`() {
        val hand = listOf(
            card(Suit.SPADES, Rank.ACE),
            card(Suit.SPADES, Rank.SEVEN),
            card(Suit.CLUBS, Rank.THREE)
        )
        val legal = BatakRules.legalMoves(hand, emptyList(), Suit.SPADES)
        assertEquals(listOf(card(Suit.CLUBS, Rank.THREE)), legal)
        assertFalse(BatakRules.isValidMove(hand, emptyList(), card(Suit.SPADES, Rank.ACE), Suit.SPADES))
    }

    @Test
    fun `koz A K Q varsa kozla baslanabilir`() {
        val hand = listOf(
            card(Suit.SPADES, Rank.ACE),
            card(Suit.SPADES, Rank.KING),
            card(Suit.SPADES, Rank.QUEEN),
            card(Suit.CLUBS, Rank.THREE)
        )
        val legal = BatakRules.legalMoves(hand, emptyList(), Suit.SPADES)
        assertEquals(4, legal.size)
        assertTrue(BatakRules.holdsAkqOfTrump(hand, Suit.SPADES))
    }

    @Test
    fun `eli tamamen koz olan oyuncu kozla baslayabilir`() {
        val hand = listOf(
            card(Suit.SPADES, Rank.TWO),
            card(Suit.SPADES, Rank.FIVE)
        )
        val legal = BatakRules.legalMoves(hand, emptyList(), Suit.SPADES)
        assertEquals(2, legal.size)
    }

    @Test
    fun `rengi yoksa koz atmak zorundadir`() {
        val hand = listOf(
            card(Suit.CLUBS, Rank.THREE),
            card(Suit.SPADES, Rank.SEVEN),
            card(Suit.SPADES, Rank.ACE)
        )
        val trick = listOf(
            PlayedCard(0, card(Suit.HEARTS, Rank.NINE)),
            PlayedCard(1, card(Suit.HEARTS, Rank.FOUR))
        )
        val legal = BatakRules.legalMoves(hand, trick, Suit.SPADES)
        assertEquals(2, legal.size)
        assertTrue(legal.all { it.suit == Suit.SPADES })
        assertFalse(BatakRules.isValidMove(hand, trick, card(Suit.CLUBS, Rank.THREE), Suit.SPADES))
        assertTrue(BatakRules.isValidMove(hand, trick, card(Suit.SPADES, Rank.SEVEN), Suit.SPADES))
    }

    @Test
    fun `kozu yoksa istedigi karti atabilir`() {
        val hand = listOf(
            card(Suit.HEARTS, Rank.KING),
            card(Suit.CLUBS, Rank.TWO)
        )
        val trick = listOf(PlayedCard(1, card(Suit.SPADES, Rank.NINE)))
        val legal = BatakRules.legalMoves(hand, trick, Suit.DIAMONDS)
        assertEquals(2, legal.size)
        assertTrue(BatakRules.isValidMove(hand, trick, card(Suit.CLUBS, Rank.TWO), Suit.DIAMONDS))
    }

    @Test
    fun `kozu buyutemiyorsa yine de koz atmak zorundadir`() {
        val hand = listOf(
            card(Suit.CLUBS, Rank.THREE),
            card(Suit.SPADES, Rank.TWO)
        )
        val trick = listOf(
            PlayedCard(0, card(Suit.HEARTS, Rank.NINE)),
            PlayedCard(1, card(Suit.SPADES, Rank.KING))
        )
        val legal = BatakRules.legalMoves(hand, trick, Suit.SPADES)
        assertEquals(listOf(card(Suit.SPADES, Rank.TWO)), legal)
    }

    @Test
    fun `esli modda es kazaniyorsa koz zorunlulugu yoktur`() {
        val hand = listOf(
            card(Suit.CLUBS, Rank.THREE),
            card(Suit.SPADES, Rank.SEVEN),
            card(Suit.SPADES, Rank.ACE)
        )
        val trick = listOf(
            PlayedCard(0, card(Suit.HEARTS, Rank.NINE)),
            PlayedCard(2, card(Suit.HEARTS, Rank.ACE))
        )
        val legal = BatakRules.legalMoves(hand, trick, Suit.SPADES, partnerWinning = true)
        assertEquals(3, legal.size)
    }

    @Test
    fun `koz masaya atildiktan sonra kozu buyutme zorunlulugu vardir`() {
        val hand = listOf(
            card(Suit.CLUBS, Rank.THREE),
            card(Suit.SPADES, Rank.SEVEN),
            card(Suit.SPADES, Rank.ACE)
        )
        val trick = listOf(
            PlayedCard(0, card(Suit.HEARTS, Rank.NINE)),
            PlayedCard(1, card(Suit.SPADES, Rank.KING))
        )
        val legal = BatakRules.legalMoves(hand, trick, Suit.SPADES)
        assertEquals(listOf(card(Suit.SPADES, Rank.ACE)), legal)
    }

    @Test
    fun `eli tamamen koz olan oyuncu koz oynayabilir`() {
        val hand = listOf(
            card(Suit.SPADES, Rank.TWO),
            card(Suit.SPADES, Rank.FIVE)
        )
        val trick = listOf(PlayedCard(0, card(Suit.HEARTS, Rank.NINE)))
        val legal = BatakRules.legalMoves(hand, trick, Suit.SPADES)
        assertEquals(2, legal.size)
    }

    @Test
    fun `bos el ve kozsuz durumda tum kartlar oynanabilir`() {
        val hand = BatakRules.deal(3L, 0)[0].toList()
        assertEquals(13, BatakRules.legalMoves(hand, emptyList(), null).size)
    }

    @Test
    fun `ele kozla baslanamaz ama koz disi tum kartlar oynanabilir`() {
        val hand = listOf(
            card(Suit.SPADES, Rank.ACE),
            card(Suit.HEARTS, Rank.TWO),
            card(Suit.CLUBS, Rank.FIVE),
            card(Suit.DIAMONDS, Rank.NINE)
        )
        val legal = BatakRules.legalMoves(hand, emptyList(), Suit.SPADES)
        assertEquals(3, legal.size)
        assertTrue(legal.none { it.suit == Suit.SPADES })
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
    fun `essiz modda puanlama ornek durumlari dogru hesaplar`() {
        val bids = listOf(9, 8, 0, 12)
        val tricks = listOf(10, 7, 2, 1)
        val scores = BatakRules.scoreRound(bids, tricks, GameMode.SOLO)
        assertEquals(10, scores[0])
        assertEquals(-8, scores[1])
        assertEquals(0, scores[2])
        assertEquals(-12, scores[3])
    }

    @Test
    fun `essiz modda tam basarili ihale el sayisini verir`() {
        val scores = BatakRules.scoreRound(listOf(8, 0, 13, 0), listOf(13, 0, 13, 0), GameMode.SOLO)
        assertEquals(13, scores[0])
        assertEquals(0, scores[1])
        assertEquals(13, scores[2])
        assertEquals(0, scores[3])
    }

    @Test
    fun `esli modda tutulan ihale iki esede arti ihale yazar`() {
        val bids = listOf(8, 0, 0, 0)
        val tricks = listOf(5, 3, 5, 0)
        val scores = BatakRules.scoreRound(bids, tricks, GameMode.PARTNERED, highestBidder = 0, highestBid = 8)
        assertEquals(8, scores[0])
        assertEquals(0, scores[1])
        assertEquals(8, scores[2])
        assertEquals(0, scores[3])
    }

    @Test
    fun `esli modda tutulamayan ihale essizlere eksi yazar`() {
        val bids = listOf(0, 9, 0, 0)
        val tricks = listOf(2, 4, 3, 3)
        val scores = BatakRules.scoreRound(bids, tricks, GameMode.PARTNERED, highestBidder = 1, highestBid = 9)
        assertEquals(9, scores[0])
        assertEquals(-9, scores[1])
        assertEquals(9, scores[2])
        assertEquals(-9, scores[3])
    }

    @Test
    fun `takim yardimcilari dogru calisir`() {
        assertTrue(BatakRules.isPartner(0, 2))
        assertTrue(BatakRules.isPartner(1, 3))
        assertFalse(BatakRules.isPartner(0, 1))
        assertEquals(0, BatakRules.teamOf(0))
        assertEquals(0, BatakRules.teamOf(2))
        assertEquals(1, BatakRules.teamOf(1))
        assertEquals(1, BatakRules.teamOf(3))
    }

    @Test
    fun `essiz modda ihale dogrulama kurallari`() {
        val mode = GameMode.SOLO
        assertTrue(BatakRules.isValidBid(0, 0, mode))
        assertTrue(BatakRules.isValidBid(5, 0, mode))
        assertTrue(BatakRules.isValidBid(6, 5, mode))
        assertFalse(BatakRules.isValidBid(5, 5, mode))
        assertFalse(BatakRules.isValidBid(4, 0, mode))
        assertFalse(BatakRules.isValidBid(14, 0, mode))
        assertFalse(BatakRules.isValidBid(13, 13, mode))
    }

    @Test
    fun `esli modda ihale 7den baslar`() {
        val mode = GameMode.PARTNERED
        assertTrue(BatakRules.isValidBid(7, 0, mode))
        assertFalse(BatakRules.isValidBid(6, 0, mode))
        assertTrue(BatakRules.isValidBid(8, 7, mode))
        assertEquals(7, BatakRules.minBid(mode))
        assertEquals(5, BatakRules.minBid(GameMode.SOLO))
    }

    @Test
    fun `gecerli teklif listesi dogru`() {
        assertEquals((5..13).toList(), BatakRules.nextBidOptions(0, GameMode.SOLO))
        assertEquals((10..13).toList(), BatakRules.nextBidOptions(9, GameMode.SOLO))
        assertEquals(emptyList<Int>(), BatakRules.nextBidOptions(13, GameMode.SOLO))
        assertEquals((7..13).toList(), BatakRules.nextBidOptions(0, GameMode.PARTNERED))
        assertEquals((12..13).toList(), BatakRules.nextBidOptions(11, GameMode.PARTNERED))
    }

    @Test
    fun `el kartlari renge ve buyukluge gore siralanir`() {
        val hand = listOf(
            card(Suit.HEARTS, Rank.TWO),
            card(Suit.SPADES, Rank.FIVE),
            card(Suit.CLUBS, Rank.ACE),
            card(Suit.HEARTS, Rank.ACE),
            card(Suit.DIAMONDS, Rank.KING),
            card(Suit.SPADES, Rank.ACE),
            card(Suit.CLUBS, Rank.TWO),
            card(Suit.DIAMONDS, Rank.FOUR)
        )
        val sorted = BatakRules.sortHand(hand)
        assertEquals(
            listOf(
                card(Suit.SPADES, Rank.ACE),
                card(Suit.SPADES, Rank.FIVE),
                card(Suit.HEARTS, Rank.ACE),
                card(Suit.HEARTS, Rank.TWO),
                card(Suit.CLUBS, Rank.ACE),
                card(Suit.CLUBS, Rank.TWO),
                card(Suit.DIAMONDS, Rank.KING),
                card(Suit.DIAMONDS, Rank.FOUR)
            ),
            sorted
        )
    }

    private fun card(suit: Suit, rank: Rank): PlayingCard = PlayingCard(suit, rank)
}
