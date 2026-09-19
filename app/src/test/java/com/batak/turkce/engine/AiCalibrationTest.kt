package com.batak.turkce.engine

import com.batak.turkce.ai.BatakAi
import com.batak.turkce.model.Difficulty
import com.batak.turkce.model.GameMode
import com.batak.turkce.model.PlayingCard
import org.junit.Test
import kotlin.random.Random

class AiCalibrationTest {

    @Test
    fun `ihale tahmin dagilimi`() {
        val random = Random(20250919)
        val ai = BatakAi(Difficulty.NORMAL, random)
        val samples = DoubleArray(30000)
        for (i in samples.indices) {
            val hands = BatakRules.deal(random.nextLong(), i % 4)
            samples[i] = ai.estimateTricks(hands[i % 4])
        }
        val sorted = samples.sorted()
        fun p(q: Double) = sorted[(q * (sorted.size - 1)).toInt()]
        println("ESTIMATE mean=%.3f p50=%.3f p70=%.3f p80=%.3f p85=%.3f p90=%.3f p95=%.3f max=%.3f"
            .format(sorted.average(), p(0.50), p(0.70), p(0.80), p(0.85), p(0.90), p(0.95), sorted.last()))
    }

    @Test
    fun `tahmin ile gercek el sayisi iliskisi`() {
        val random = Random(777)
        val ais = List(4) { BatakAi(Difficulty.NORMAL, Random(random.nextLong())) }
        val estimateToTricks = mutableMapOf<Int, MutableList<Int>>()
        var totalTricks = 0

        repeat(2500) { iteration ->
            var hands = BatakRules.deal(random.nextLong(), iteration % 4)
            val estimates = hands.map { ais[0].estimateTricks(it) }
            val trump = pickBestGlobalTrump(hands)
            val mutableHands = hands.map { it.toMutableList() }
            val tricksWon = MutableList(4) { 0 }
            val playedCards = mutableListOf<PlayingCard>()
            val completed = mutableListOf<List<PlayedCard>>()
            var leader = 0
            repeat(13) {
                val trick = mutableListOf<PlayedCard>()
                var player = leader
                repeat(4) {
                    val state = BatakGame(
                        hands = mutableHands.map { h -> h.toList() },
                        phase = GamePhase.PLAYING,
                        trump = trump,
                        currentPlayer = player,
                        trick = trick.toList(),
                        completedTricks = completed.toList(),
                        playedCards = playedCards.toList()
                    )
                    val card = ais[player].chooseCard(state, player)
                    mutableHands[player].remove(card)
                    trick.add(PlayedCard(player, card))
                    playedCards.add(card)
                    player = (player + 1) % 4
                }
                val winner = trick[BatakRules.trickWinner(trick, trump)].player
                tricksWon[winner]++
                completed.add(trick.toList())
                leader = winner
            }
            for (i in 0..3) {
                totalTricks += tricksWon[i]
                val bucket = (estimates[i] * 2).toInt()
                estimateToTricks.getOrPut(bucket) { mutableListOf() }.add(tricksWon[i])
            }
            hands = emptyList()
        }

        println("GERCEK ortalama el = %.3f".format(totalTricks.toDouble() / (2500 * 4)))
        val buckets = estimateToTricks.keys.sorted()
        for (bucket in buckets) {
            val list = estimateToTricks.getValue(bucket)
            if (list.size < 30) continue
            val avg = list.average()
            val p8 = list.count { it >= 8 }.toDouble() / list.size
            val p9 = list.count { it >= 9 }.toDouble() / list.size
            println("est[%.1f-%.1f] n=%d ortEl=%.2f P(8+)=%.3f P(9+)=%.3f"
                .format(bucket / 2.0, (bucket + 1) / 2.0, list.size, avg, p8, p9))
        }
    }

    @Test
    fun `ihale akisi all pass orani`() {
        for (mode in listOf(GameMode.SOLO, GameMode.PARTNERED)) {
            for (difficulty in listOf(Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD)) {
                val rng = Random(99)
                val ais = List(4) { BatakAi(difficulty, Random(rng.nextLong())) }
                var forced = 0
                val total = 4000
                repeat(total) { i ->
                    val dealer = i % 4
                    val hands = BatakRules.deal(rng.nextLong(), dealer)
                    val bids = MutableList<Int?>(4) { null }
                    var best = 0
                    var bestPlayer = -1
                    for (k in 0 until 4) {
                        val p = (dealer + 1 + k) % 4
                        val bid = ais[p].decideBid(
                            hand = hands[p],
                            highestBid = best,
                            mode = mode,
                            highestBidder = bestPlayer,
                            myPlayer = p
                        )
                        bids[p] = bid
                        if (bid > best) {
                            best = bid
                            bestPlayer = p
                        }
                    }
                    if (bestPlayer < 0) forced++
                }
                println("MODE=$mode DIFF=$difficulty allPassForcedRate=%.3f".format(forced.toDouble() / total))
            }
        }
    }

    private fun pickBestGlobalTrump(hands: List<List<PlayingCard>>): com.batak.turkce.model.Suit {
        val ai = BatakAi(Difficulty.NORMAL, Random(1))
        return hands.maxBy { ai.estimateTricks(it) }.let { ai.chooseTrump(it) }
    }
}
