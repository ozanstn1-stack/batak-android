package com.batak.turkce.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batak.turkce.BatakApplication
import com.batak.turkce.R
import com.batak.turkce.ai.BatakAi
import com.batak.turkce.data.AppSettings
import com.batak.turkce.data.UserStats
import com.batak.turkce.engine.BatakGame
import com.batak.turkce.engine.BatakRules
import com.batak.turkce.engine.GamePhase
import com.batak.turkce.engine.PlayedCard
import com.batak.turkce.engine.RoundResult
import com.batak.turkce.model.AppTheme
import com.batak.turkce.model.CardDesign
import com.batak.turkce.model.Difficulty
import com.batak.turkce.model.GameMode
import com.batak.turkce.model.PlayingCard
import com.batak.turkce.model.Suit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BatakApplication
    private val repo = app.repository
    private val audio = app.audio
    private val vibration = app.vibration

    private val _game = MutableStateFlow<BatakGame?>(null)
    val game: StateFlow<BatakGame?> = _game.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _stats = MutableStateFlow(UserStats())
    val stats: StateFlow<UserStats> = _stats.asStateFlow()

    private val _hasSave = MutableStateFlow(false)
    val hasSave: StateFlow<Boolean> = _hasSave.asStateFlow()

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    private val rng = Random(System.nanoTime())
    private var ais: List<BatakAi> = emptyList()
    private var aiJob: Job? = null
    private var dealJob: Job? = null
    private var toastJob: Job? = null
    private var cachedSave: BatakGame? = null

    init {
        viewModelScope.launch {
            repo.settingsFlow.collect { settings ->
                _settings.value = settings
                audio.enabled = settings.soundEnabled
                vibration.enabled = settings.vibrationEnabled
            }
        }
        viewModelScope.launch { repo.statsFlow.collect { _stats.value = it } }
        viewModelScope.launch {
            repo.saveFlow.collect {
                cachedSave = it
                _hasSave.value = it != null
            }
        }
    }

    // ------------------------------------------------------------- UYGULAMA

    fun startNewMatch() {
        val s = _settings.value
        ais = List(4) { BatakAi(s.difficulty, Random(rng.nextLong())) }
        val game = BatakGame(
            playerName = s.playerName.ifBlank { "Oyuncu" },
            difficulty = s.difficulty,
            mode = s.gameMode,
            totalRounds = s.roundsPerGame,
            roundNumber = 1,
            dealer = rng.nextInt(4)
        )
        _game.value = game
        startDeal(game, redeal = false)
    }

    fun resumeSavedGame() {
        val saved = cachedSave ?: return
        ais = List(4) { BatakAi(saved.difficulty, Random(rng.nextLong())) }
        _game.value = saved
        when (saved.phase) {
            GamePhase.DEALING -> startDeal(saved, redeal = false, keepHands = true)
            GamePhase.ROUND_OVER, GamePhase.GAME_OVER -> Unit
            else -> pump()
        }
    }

    fun discardGame() {
        cancelJobs()
        _game.value = null
        cachedSave = null
        _hasSave.value = false
        viewModelScope.launch { repo.clearSave() }
    }

    // ---------------------------------------------------------------- DAGITIM

    private fun startDeal(previous: BatakGame, redeal: Boolean, keepHands: Boolean = false) {
        cancelJobs()
        val hands = if (keepHands && previous.hands.size == 4 && previous.hands.all { it.size == BatakRules.HAND_SIZE }) {
            previous.hands
        } else {
            BatakRules.deal(rng.nextLong(), previous.dealer)
        }
        val game = previous.copy(
            hands = hands,
            phase = GamePhase.DEALING,
            dealId = previous.dealId + 1,
            bids = List(4) { null },
            bidTurn = (previous.dealer + 1) % 4,
            highestBid = 0,
            highestBidder = -1,
            trump = null,
            currentPlayer = -1,
            trick = emptyList(),
            tricksWon = List(4) { 0 },
            completedTricks = emptyList(),
            playedCards = emptyList(),
            lastTrickWinner = -1,
            roundResult = null,
            redealCount = if (redeal) previous.redealCount + 1 else 0
        )
        update(game)
        val animated = _settings.value.animationsEnabled
        dealJob = viewModelScope.launch {
            val count = if (animated) BatakRules.HAND_SIZE else 3
            val totalMs = if (animated) 1250L else 350L
            val gap = totalMs / count
            repeat(count) {
                audio.play(R.raw.card_deal, volume = 0.65f, rate = 0.94f + rng.nextFloat() * 0.12f)
                delay(gap)
            }
            val current = _game.value ?: return@launch
            if (current.phase == GamePhase.DEALING && current.dealId == game.dealId) {
                update(current.copy(phase = GamePhase.BIDDING))
                pump()
            }
        }
    }

    // ---------------------------------------------------------------- IHALE

    fun canHumanBid(value: Int): Boolean {
        val game = _game.value ?: return false
        return game.phase == GamePhase.BIDDING && game.bidTurn == 0 &&
            BatakRules.isValidBid(value, game.highestBid, game.mode)
    }

    fun humanBid(value: Int) {
        if (!canHumanBid(value)) {
            showToast("Bu ihale şu an geçerli değil.")
            return
        }
        audio.play(R.raw.button_click, 0.8f)
        applyBid(0, value)
    }

    private fun applyBid(player: Int, bid: Int) {
        val game = _game.value ?: return
        if (game.phase != GamePhase.BIDDING) return
        val valid = if (bid == 0) 0 else bid.takeIf { BatakRules.isValidBid(it, game.highestBid, game.mode) } ?: 0
        val bids = game.bids.toMutableList()
        bids[player] = valid
        var highestBid = game.highestBid
        var highestBidder = game.highestBidder
        if (valid > highestBid) {
            highestBid = valid
            highestBidder = player
        }
        if (player != 0) audio.play(R.raw.bid, 0.55f)
        val bidsPlaced = bids.count { it != null }
        if (bidsPlaced >= 4) {
            if (highestBidder == -1) {
                update(game.copy(bids = bids))
                showToast("Herkes pas geçti. Kartlar yeniden dağıtılıyor.")
                viewModelScope.launch {
                    delay(1300)
                    val current = _game.value ?: return@launch
                    if (current.phase == GamePhase.BIDDING && current.bids.count { it != null } >= 4) {
                        startDeal(current, redeal = true)
                    }
                }
                return
            }
            val next = game.copy(bids = bids, highestBid = highestBid, highestBidder = highestBidder, phase = GamePhase.TRUMP_SELECTION)
            update(next)
            val bidderName = nameOf(highestBidder)
            if (highestBidder != 0) showToast("$bidderName ${highestBid} dedi. Koz seçiyor...")
            pump()
        } else {
            val next = game.copy(
                bids = bids,
                highestBid = highestBid,
                highestBidder = highestBidder,
                bidTurn = (game.bidTurn + 1) % BatakRules.PLAYER_COUNT
            )
            update(next)
            pump()
        }
    }

    // ------------------------------------------------------------------ KOZ

    fun humanChooseTrump(suit: Suit) {
        val game = _game.value ?: return
        if (game.phase != GamePhase.TRUMP_SELECTION || game.highestBidder != 0) return
        audio.play(R.raw.button_click, 0.8f)
        applyTrump(0, suit)
    }

    private fun applyTrump(player: Int, suit: Suit) {
        val game = _game.value ?: return
        if (game.phase != GamePhase.TRUMP_SELECTION) return
        val next = game.copy(
            trump = suit,
            phase = GamePhase.PLAYING,
            currentPlayer = game.highestBidder,
            trick = emptyList()
        )
        update(next)
        showToast("Koz: ${suit.labelTr}")
        if (player != 0) audio.play(R.raw.bid, 0.7f)
        pump()
    }

    // ---------------------------------------------------------- KART OYNAMA

    fun humanPlayCard(card: PlayingCard) {
        val game = _game.value ?: return
        if (game.phase != GamePhase.PLAYING || game.currentPlayer != 0) return
        val hand = game.hands[0]
        if (!BatakRules.isValidMove(hand, game.trick, card, game.trump)) {
            val trumpBlocked = game.trump != null &&
                card.suit == game.trump &&
                game.trick.isNotEmpty() &&
                game.trick.none { it.card.suit == game.trump } &&
                hand.any { it.suit != game.trump }
            showToast(
                if (trumpBlocked) "Masaya koz atılmadan koz oynayamazsın."
                else "Bu kart şu an oynanamaz."
            )
            vibration.light()
            return
        }
        applyPlay(0, card)
    }

    private fun applyPlay(player: Int, card: PlayingCard) {
        val game = _game.value ?: return
        if (game.phase != GamePhase.PLAYING || game.currentPlayer != player) return
        val hand = game.hands[player]
        val legal = BatakRules.legalMoves(hand, game.trick, game.trump)
        val chosen = if (card in legal) card else legal.firstOrNull() ?: return
        val hands = game.hands.toMutableList()
        hands[player] = hand - chosen
        val trick = game.trick + PlayedCard(player, chosen)
        audio.play(R.raw.card_play, if (player == 0) 0.9f else 0.6f)
        if (player == 0) vibration.light()

        if (trick.size == BatakRules.TRICK_SIZE) {
            val winnerIndex = BatakRules.trickWinner(trick, game.trump)
            val winner = trick[winnerIndex].player
            val next = game.copy(
                hands = hands,
                trick = trick,
                playedCards = game.playedCards + chosen,
                phase = GamePhase.TRICK_RESOLUTION,
                lastTrickWinner = winner
            )
            update(next)
            if (winner == 0) {
                audio.play(R.raw.trick_win, 0.75f)
                vibration.double()
            }
        } else {
            val next = game.copy(
                hands = hands,
                trick = trick,
                playedCards = game.playedCards + chosen,
                currentPlayer = (player + 1) % BatakRules.PLAYER_COUNT
            )
            update(next)
        }
        pump()
    }

    private fun resolveTrick() {
        val game = _game.value ?: return
        if (game.phase != GamePhase.TRICK_RESOLUTION) return
        val winner = game.lastTrickWinner
        if (winner < 0) {
            update(game.copy(phase = GamePhase.PLAYING, currentPlayer = (game.currentPlayer + 1) % BatakRules.PLAYER_COUNT))
            pump()
            return
        }
        val tricksWon = game.tricksWon.toMutableList()
        tricksWon[winner] = tricksWon[winner] + 1
        val next = game.copy(
            tricksWon = tricksWon,
            completedTricks = game.completedTricks + listOf(game.trick),
            trick = emptyList(),
            currentPlayer = winner,
            phase = GamePhase.PLAYING,
            lastTrickWinner = -1
        )
        if (next.hands.all { it.isEmpty() }) {
            finishRound(next)
        } else {
            update(next)
            pump()
        }
    }

    private fun finishRound(game: BatakGame) {
        val bids = game.bids.map { it ?: 0 }
        val roundScores = BatakRules.scoreRound(
            bids = bids,
            tricksWon = game.tricksWon,
            mode = game.mode,
            highestBidder = game.highestBidder,
            highestBid = game.highestBid
        )
        val trump = game.trump ?: Suit.SPADES
        val next = game.copy(
            scores = game.scores.zip(roundScores) { a, b -> a + b },
            cumulativeTricks = game.cumulativeTricks.zip(game.tricksWon) { a, b -> a + b },
            roundResult = RoundResult(bids = bids, tricksWon = game.tricksWon, scores = roundScores, trump = trump),
            phase = GamePhase.ROUND_OVER
        )
        update(next)
        val humanRound = roundScores[0]
        if (humanRound > 0) audio.play(R.raw.round_win, 0.8f) else if (humanRound < 0) {
            audio.play(R.raw.round_lose, 0.8f)
            vibration.medium()
        } else {
            audio.play(R.raw.trick_win, 0.6f)
        }
    }

    fun continueAfterRound() {
        val game = _game.value ?: return
        if (game.phase != GamePhase.ROUND_OVER) return
        audio.play(R.raw.button_click, 0.8f)
        if (game.roundNumber >= game.totalRounds) {
            finishMatch(game)
        } else {
            startDeal(game.copy(roundNumber = game.roundNumber + 1, dealer = (game.dealer + 1) % BatakRules.PLAYER_COUNT), redeal = false)
        }
    }

    private fun finishMatch(game: BatakGame) {
        val humanScore = game.scores[0]
        val won = when (game.mode) {
            GameMode.SOLO -> humanScore >= (game.scores.drop(1).maxOrNull() ?: 0)
            GameMode.PARTNERED -> {
                val myTeam = BatakRules.teamOf(0)
                val myTeamScore = game.scores[0] + game.scores[2]
                val otherTeamScore = game.scores[1] + game.scores[3]
                if (myTeam == 0) myTeamScore >= otherTeamScore else myTeamScore > otherTeamScore
            }
        }
        val finished = game.copy(phase = GamePhase.GAME_OVER, statsApplied = true)
        update(finished, persist = false)
        viewModelScope.launch { repo.clearSave() }
        if (won) audio.play(R.raw.round_win, 1f) else {
            audio.play(R.raw.round_lose, 1f)
            vibration.medium()
        }
        viewModelScope.launch {
            repo.recordMatchFinished(
                humanScore = humanScore,
                won = won,
                humanTricks = game.cumulativeTricks.getOrElse(0) { 0 }
            )
        }
    }

    fun backToMenuKeepGame() {
        val game = _game.value
        if (game != null && game.phase != GamePhase.GAME_OVER) {
            viewModelScope.launch { repo.saveGame(game) }
        }
    }

    fun invalidCardFeedback() {
        showToast("Bu kart şu an oynanamaz.")
        vibration.light()
    }

    fun playClick() {
        audio.play(R.raw.button_click, 0.8f)
    }

    // ------------------------------------------------------------ YAPAY ZEKA

    private fun pump() {
        if (aiJob?.isActive == true) return
        aiJob = viewModelScope.launch {
            while (true) {
                val game = _game.value ?: break
                when (game.phase) {
                    GamePhase.BIDDING -> {
                        val player = game.bidTurn
                        if (player == 0) break
                        delay(thinkDelay())
                        val current = _game.value ?: break
                        if (current.phase != GamePhase.BIDDING || current.bidTurn != player) break
                        val bidder = ais.getOrNull(player) ?: break
                        val bid = bidder.decideBid(
                            hand = current.hands[player],
                            highestBid = current.highestBid,
                            lastToBid = player == current.dealer,
                            passedCount = current.bids.count { it == 0 },
                            mode = current.mode,
                            highestBidder = current.highestBidder,
                            myPlayer = player
                        )
                        applyBid(player, bid)
                    }
                    GamePhase.TRUMP_SELECTION -> {
                        val player = game.highestBidder
                        if (player <= 0) break
                        delay(850)
                        val current = _game.value ?: break
                        if (current.phase != GamePhase.TRUMP_SELECTION || current.highestBidder != player) break
                        val chooser = ais.getOrNull(player) ?: break
                        applyTrump(player, chooser.chooseTrump(current.hands[player]))
                    }
                    GamePhase.PLAYING -> {
                        val player = game.currentPlayer
                        if (player == 0) break
                        delay(thinkDelay())
                        val current = _game.value ?: break
                        if (current.phase != GamePhase.PLAYING || current.currentPlayer != player) break
                        val actor = ais.getOrNull(player) ?: break
                        val card = runCatching { actor.chooseCard(current, player) }.getOrNull() ?: break
                        applyPlay(player, card)
                    }
                    GamePhase.TRICK_RESOLUTION -> {
                        delay(if (_settings.value.animationsEnabled) 1050L else 380L)
                        val current = _game.value ?: break
                        if (current.phase != GamePhase.TRICK_RESOLUTION) break
                        resolveTrick()
                    }
                    else -> break
                }
            }
        }
    }

    private fun thinkDelay(): Long {
        val base = when (_settings.value.difficulty) {
            Difficulty.EASY -> 900L
            Difficulty.NORMAL -> 780L
            Difficulty.HARD -> 640L
        }
        return if (_settings.value.animationsEnabled) base + rng.nextLong(0, 260) else 260L
    }

    // ------------------------------------------------------------- AYARLAR

    fun setSoundEnabled(value: Boolean) = viewModelScope.launch { repo.updateSettings { it.copy(soundEnabled = value) } }
    fun setVibrationEnabled(value: Boolean) = viewModelScope.launch { repo.updateSettings { it.copy(vibrationEnabled = value) } }
    fun setAnimationsEnabled(value: Boolean) = viewModelScope.launch { repo.updateSettings { it.copy(animationsEnabled = value) } }
    fun setDifficulty(value: Difficulty) = viewModelScope.launch { repo.updateSettings { it.copy(difficulty = value) } }
    fun setGameMode(value: GameMode) = viewModelScope.launch { repo.updateSettings { it.copy(gameMode = value) } }
    fun setCardDesign(value: CardDesign) = viewModelScope.launch { repo.updateSettings { it.copy(cardDesign = value) } }
    fun setTheme(value: AppTheme) = viewModelScope.launch { repo.updateSettings { it.copy(theme = value) } }
    fun setPlayerName(value: String) = viewModelScope.launch { repo.updateSettings { it.copy(playerName = value.take(16)) } }
    fun setRoundsPerGame(value: Int) = viewModelScope.launch { repo.updateSettings { it.copy(roundsPerGame = value.coerceIn(1, 20)) } }
    fun resetStats() = viewModelScope.launch { repo.resetStats() }

    // -------------------------------------------------------------- YARDIMCI

    fun nameOf(player: Int): String = when (player) {
        0 -> _game.value?.playerName ?: _settings.value.playerName
        1 -> "Rakip 1"
        2 -> "Rakip 2"
        else -> "Rakip 3"
    }

    private fun update(game: BatakGame, persist: Boolean = true) {
        _game.value = game
        if (persist && game.phase != GamePhase.GAME_OVER) {
            viewModelScope.launch { repo.saveGame(game) }
        }
    }

    private fun showToast(message: String) {
        _toast.value = message
        toastJob?.cancel()
        toastJob = viewModelScope.launch {
            delay(2300)
            _toast.value = null
        }
    }

    private fun cancelJobs() {
        aiJob?.cancel()
        dealJob?.cancel()
        aiJob = null
        dealJob = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelJobs()
    }
}
