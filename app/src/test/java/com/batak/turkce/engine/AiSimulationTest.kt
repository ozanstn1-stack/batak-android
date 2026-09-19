package com.batak.turkce.engine

import com.batak.turkce.ai.BatakAi
import com.batak.turkce.model.Difficulty
import com.batak.turkce.model.GameMode
import com.batak.turkce.model.PlayingCard
import com.batak.turkce.model.Rank
import com.batak.turkce.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AiSimulationTest {

    @Test
    fun `tum zorluk seviyeleri essiz modda gecerli hamle yapar`() {
        var rounds = 0
        for (seed in 1L..30L) {
            val difficulties = listOf(Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD, Difficulty.NORMAL)
            rounds += simulateMatch(seed, difficulties, GameMode.SOLO)
        }
        assertTrue("En az 100 el oynanmis olmali", rounds >= 100)
    }

    @Test
    fun `esli modda oyun tamamlanir ve hamleler gecerli`() {
        var rounds = 0
        for (seed in 100L..124L) {
            val difficulties = listOf(Difficulty.NORMAL, Difficulty.NORMAL, Difficulty.HARD, Difficulty.EASY)
            rounds += simulateMatch(seed, difficulties, GameMode.PARTNERED)
        }
        assertTrue("En az 80 el oynanmis olmali", rounds >= 80)
    }

    @Test
    fun `ayni tohum ayni sonucu uretir`() {
        val first = simulateMatch(42L, List(4) { Difficulty.HARD }, GameMode.SOLO)
        val second = simulateMatch(42L, List(4) { Difficulty.HARD }, GameMode.SOLO)
        assertEquals(first, second)
    }

    @Test
    fun `guclu el ihale verir`() {
        val strong = listOf(
            PlayingCard(Suit.SPADES, Rank.ACE),
            PlayingCard(Suit.SPADES, Rank.KING),
            PlayingCard(Suit.SPADES, Rank.QUEEN),
            PlayingCard(Suit.SPADES, Rank.JACK),
            PlayingCard(Suit.SPADES, Rank.TEN),
            PlayingCard(Suit.HEARTS, Rank.ACE),
            PlayingCard(Suit.HEARTS, Rank.KING),
            PlayingCard(Suit.HEARTS, Rank.QUEEN),
            PlayingCard(Suit.DIAMONDS, Rank.ACE),
            PlayingCard(Suit.DIAMONDS, Rank.KING),
            PlayingCard(Suit.CLUBS, Rank.ACE),
            PlayingCard(Suit.CLUBS, Rank.KING),
            PlayingCard(Suit.CLUBS, Rank.TWO)
        )
        val ai = BatakAi(Difficulty.HARD, Random(1))
        val estimate = ai.estimateTricks(strong)
        assertTrue("Guclu el en az 8 el tahmin etmeli: $estimate", estimate >= 8.0)
        val bid = ai.decideBid(strong, 0, GameMode.SOLO, -1, 0)
        assertEquals("Essiz modda minimum ihale 5 olmali", BatakRules.MIN_BID_SOLO, bid)
        val partneredBid = ai.decideBid(strong, 0, GameMode.PARTNERED, -1, 0)
        assertEquals(BatakRules.MIN_BID_PARTNERED, partneredBid)
    }

    @Test
    fun `zayif el genelde pas gecer`() {
        val weak = listOf(
            PlayingCard(Suit.SPADES, Rank.TWO),
            PlayingCard(Suit.SPADES, Rank.THREE),
            PlayingCard(Suit.SPADES, Rank.FOUR),
            PlayingCard(Suit.SPADES, Rank.FIVE),
            PlayingCard(Suit.HEARTS, Rank.TWO),
            PlayingCard(Suit.HEARTS, Rank.THREE),
            PlayingCard(Suit.HEARTS, Rank.FOUR),
            PlayingCard(Suit.DIAMONDS, Rank.TWO),
            PlayingCard(Suit.DIAMONDS, Rank.THREE),
            PlayingCard(Suit.CLUBS, Rank.TWO),
            PlayingCard(Suit.CLUBS, Rank.THREE),
            PlayingCard(Suit.CLUBS, Rank.FOUR),
            PlayingCard(Suit.CLUBS, Rank.FIVE)
        )
        val ai = BatakAi(Difficulty.HARD, Random(2))
        assertEquals(0, ai.decideBid(weak, 0, GameMode.SOLO, -1, 0))
    }

    @Test
    fun `koz secimi en guclu rengi secer`() {
        val hand = listOf(
            PlayingCard(Suit.HEARTS, Rank.ACE),
            PlayingCard(Suit.HEARTS, Rank.KING),
            PlayingCard(Suit.HEARTS, Rank.QUEEN),
            PlayingCard(Suit.HEARTS, Rank.JACK),
            PlayingCard(Suit.HEARTS, Rank.TEN),
            PlayingCard(Suit.HEARTS, Rank.NINE),
            PlayingCard(Suit.SPADES, Rank.TWO),
            PlayingCard(Suit.SPADES, Rank.THREE),
            PlayingCard(Suit.DIAMONDS, Rank.TWO),
            PlayingCard(Suit.DIAMONDS, Rank.THREE),
            PlayingCard(Suit.CLUBS, Rank.TWO),
            PlayingCard(Suit.CLUBS, Rank.THREE),
            PlayingCard(Suit.CLUBS, Rank.FOUR)
        )
        val ai = BatakAi(Difficulty.NORMAL, Random(3))
        assertEquals(Suit.HEARTS, ai.chooseTrump(hand))
    }

    private fun simulateMatch(seed: Long, difficulties: List<Difficulty>, mode: GameMode): Int {
        val rng = Random(seed)
        val ais = difficulties.map { BatakAi(it, Random(rng.nextLong())) }
        var dealer = rng.nextInt(4)
        var roundsPlayed = 0

        repeat(5) {
            val dealt = BatakRules.deal(rng.nextLong(), dealer)
            val bidList = MutableList<Int?>(4) { null }
            var best = 0
            var bestPlayer = -1
            for (i in 0 until 4) {
                val player = (dealer + 1 + i) % 4
                val bid = ais[player].decideBid(
                    hand = dealt[player],
                    highestBid = best,
                    mode = mode,
                    highestBidder = bestPlayer,
                    myPlayer = player
                )
                assertTrue(
                    "Gecersiz ihale: $bid (en yuksek: $best)",
                    bid == 0 || (bid in BatakRules.minBid(mode)..BatakRules.MAX_BID && bid > best)
                )
                bidList[player] = bid
                if (bid > best) {
                    best = bid
                    bestPlayer = player
                }
            }
            if (bestPlayer < 0) {
                bestPlayer = (dealer + 1) % 4
                best = BatakRules.forcedBid(mode)
                bidList[bestPlayer] = best
            }
            val hands: List<List<PlayingCard>> = dealt
            val bids: List<Int> = bidList.map { it ?: 0 }
            val highestBid: Int = best
            val highestBidder: Int = bestPlayer

            val trump = ais[highestBidder].chooseTrump(hands[highestBidder])
            val mutableHands = hands.map { it.toMutableList() }
            val tricksWon = MutableList(4) { 0 }
            val playedCards = mutableListOf<PlayingCard>()
            val completed = mutableListOf<List<PlayedCard>>()
            var leader = highestBidder

            repeat(BatakRules.HAND_SIZE) {
                val trick = mutableListOf<PlayedCard>()
                var currentPlayer = leader
                repeat(BatakRules.TRICK_SIZE) {
                    val state = BatakGame(
                        hands = mutableHands.map { hand -> hand.toList() },
                        phase = GamePhase.PLAYING,
                        mode = mode,
                        trump = trump,
                        currentPlayer = currentPlayer,
                        trick = trick.toList(),
                        completedTricks = completed.toList(),
                        playedCards = playedCards.toList(),
                        bids = bids,
                        highestBid = highestBid,
                        highestBidder = highestBidder
                    )
                    val card = ais[currentPlayer].chooseCard(state, currentPlayer)
                    assertTrue(
                        "AI gecersiz kart secti: $card, el: ${mutableHands[currentPlayer]}, masa: $trick",
                        BatakRules.isValidMove(mutableHands[currentPlayer], trick, card, trump)
                    )
                    mutableHands[currentPlayer].remove(card)
                    trick.add(PlayedCard(currentPlayer, card))
                    playedCards.add(card)
                    currentPlayer = (currentPlayer + 1) % 4
                }
                val winner = trick[BatakRules.trickWinner(trick, trump)].player
                tricksWon[winner]++
                completed.add(trick.toList())
                leader = winner
            }

            assertEquals(13, tricksWon.sum())
            assertTrue(mutableHands.all { it.isEmpty() })
            assertEquals(52, playedCards.size)
            assertEquals(52, playedCards.toSet().size)
            val scores = BatakRules.scoreRound(bids, tricksWon, mode, highestBidder, highestBid)
            assertEquals(4, scores.size)
            if (mode == GameMode.PARTNERED) {
                assertEquals(scores[0], scores[2])
                assertEquals(scores[1], scores[3])
            }

            roundsPlayed++
            dealer = (dealer + 1) % 4
        }
        return roundsPlayed
    }
}
