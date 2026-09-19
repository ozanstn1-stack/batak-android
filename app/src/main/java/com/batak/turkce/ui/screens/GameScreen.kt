package com.batak.turkce.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batak.turkce.engine.BatakGame
import com.batak.turkce.engine.BatakRules
import com.batak.turkce.engine.GamePhase
import com.batak.turkce.engine.PlayedCard
import com.batak.turkce.game.GameViewModel
import com.batak.turkce.model.CardDesign
import com.batak.turkce.model.GameMode
import com.batak.turkce.model.PlayingCard
import com.batak.turkce.ui.components.CardBack
import com.batak.turkce.ui.components.CardFace
import com.batak.turkce.ui.components.InfoChip
import com.batak.turkce.ui.components.TableBackground
import com.batak.turkce.ui.theme.BatakColors
import java.util.Locale

@Composable
fun GameScreen(vm: GameViewModel, onExit: () -> Unit) {
    val gameState by vm.game.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()
    val game = gameState

    if (game == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(BatakColors.FeltDeep),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BatakColors.Gold)
        }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1800)
            if (vm.game.value == null) onExit()
        }
        return
    }

    val animated = settings.animationsEnabled
    var selected by remember { mutableStateOf<PlayingCard?>(null) }

    LaunchedEffect(game.currentPlayer, game.phase, game.trick.size, game.dealId) {
        val humanHand = game.hands.getOrElse(0) { emptyList() }
        if (game.phase != GamePhase.PLAYING || game.currentPlayer != 0) {
            selected = null
        } else if (selected != null && selected !in humanHand) {
            selected = null
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val width = maxWidth
        val screenHeight = maxHeight
        val compact = maxHeight < 640.dp
        val handCardW = (width * 0.145f).coerceIn(46.dp, 66.dp)
        val handCardH = handCardW * 1.44f
        val trickCardW = (width * 0.155f).coerceIn(48.dp, 68.dp)
        val trickCardH = trickCardW * 1.44f
        val miniCardW = if (compact) 14.dp else 17.dp
        val miniCardH = miniCardW * 1.45f
        val density = LocalDensity.current

        val dealProgress = remember { Animatable(1f) }
        LaunchedEffect(game.dealId, animated) {
            if (game.phase == GamePhase.DEALING && animated) {
                dealProgress.snapTo(0f)
                dealProgress.animateTo(1f, tween(1500, easing = LinearEasing))
            } else {
                dealProgress.snapTo(1f)
            }
        }

        val legalSet = remember(game.hands, game.trick, game.trump, game.mode, game.phase, game.currentPlayer) {
            if (game.phase == GamePhase.PLAYING && game.currentPlayer == 0) {
                BatakRules.legalMoves(
                    hand = game.hands.getOrElse(0) { emptyList() },
                    trick = game.trick,
                    trump = game.trump,
                    partnerWinning = BatakRules.partnerWinning(game.trick, game.trump, 0, game.mode)
                ).toSet()
            } else {
                emptySet()
            }
        }

        TableBackground {
            // -------------------------------------------------- ust bilgi cubugu
            Row(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuPill {
                    vm.playClick()
                    vm.backToMenuKeepGame()
                    onExit()
                }
                Spacer(Modifier.width(6.dp))
                InfoChip("El: ${game.tricksPlayed} / ${BatakRules.HAND_SIZE}", fontSize = 11)
                Spacer(Modifier.weight(1f))
                InfoChip(
                    text = game.trump?.let { "KOZ: ${it.labelTr.uppercase(Locale.forLanguageTag("tr"))}" }
                        ?: "KOZ: —",
                    background = if (game.trump != null) BatakColors.Gold.copy(alpha = 0.92f) else Color.Black.copy(alpha = 0.35f),
                    textColor = if (game.trump != null) Color(0xFF3A2A06) else BatakColors.Cream,
                    fontSize = 11
                )
                Spacer(Modifier.width(6.dp))
                InfoChip(
                    text = if (game.highestBidder >= 0) "İhale: ${game.highestBid}" else "İhale: —",
                    fontSize = 11
                )
            }

            // ------------------------------------------------------------- koltuklar
            SeatView(
                playerIndex = 2,
                game = game,
                vm = vm,
                miniCardW = miniCardW,
                miniCardH = miniCardH,
                design = settings.cardDesign,
                dealProgress = dealProgress.value,
                animated = animated,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (compact) 40.dp else 48.dp)
            )
            SeatView(
                playerIndex = 1,
                game = game,
                vm = vm,
                miniCardW = miniCardW,
                miniCardH = miniCardH,
                design = settings.cardDesign,
                dealProgress = dealProgress.value,
                animated = animated,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp)
                    .offset(y = -70.dp)
            )
            SeatView(
                playerIndex = 3,
                game = game,
                vm = vm,
                miniCardW = miniCardW,
                miniCardH = miniCardH,
                design = settings.cardDesign,
                dealProgress = dealProgress.value,
                animated = animated,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .offset(y = -70.dp)
            )

            // ------------------------------------------------------------- masa ortasi
            Box(
                Modifier
                    .align(Alignment.Center)
                    .offset(y = -14.dp)
                    .size(trickCardW * 3.6f, trickCardH * 2.6f)
            ) {
                game.trick.forEach { play ->
                    key(play.player) {
                        TrickCardView(
                            play = play,
                            anchor = anchorVector(play.player, width, screenHeight),
                            cardW = trickCardW,
                            cardH = trickCardH,
                            design = settings.cardDesign,
                            isWinner = play.player == game.lastTrickWinner && game.phase == GamePhase.TRICK_RESOLUTION,
                            winnerPhase = game.phase == GamePhase.TRICK_RESOLUTION,
                            animations = animated,
                            modifier = Modifier.align(alignmentFor(play.player))
                        )
                    }
                }
            }

            if (game.phase == GamePhase.DEALING) {
                val deckAlpha = ((1f - dealProgress.value) * 2.2f).coerceIn(0f, 1f)
                val deckScale = 1f - dealProgress.value * 0.3f
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .offset(y = -14.dp)
                        .graphicsLayer {
                            alpha = deckAlpha
                            scaleX = deckScale
                            scaleY = deckScale
                        }
                ) {
                    CardBack(trickCardW, trickCardH, settings.cardDesign)
                }
            }

            // -------------------------------------------------------- alt bilgi ve el
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                if (game.phase == GamePhase.BIDDING && game.bidTurn == 0) {
                    BidPanel(
                        highestBid = game.highestBid,
                        highestBidderName = if (game.highestBidder >= 0) vm.nameOf(game.highestBidder) else null,
                        minBid = BatakRules.minBid(game.mode),
                        canBid = { vm.canHumanBid(it) },
                        onBid = { vm.humanBid(it) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                if (game.phase == GamePhase.TRUMP_SELECTION && game.highestBidder == 0) {
                    TrumpPanel(
                        bid = game.highestBid,
                        onPick = { vm.humanChooseTrump(it) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                if (game.phase == GamePhase.BIDDING && game.bidTurn != 0) {
                    StatusPill(
                        text = buildBidStatus(game, vm),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 6.dp)
                    )
                }
                if (game.phase == GamePhase.TRUMP_SELECTION && game.highestBidder != 0) {
                    StatusPill(
                        text = "${vm.nameOf(game.highestBidder)} koz seçiyor...",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 6.dp)
                    )
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip(
                        text = if (game.mode == GameMode.PARTNERED) {
                            "${game.playerName} & Rakip 2 · ${game.tricksWon.getOrElse(0) { 0 }} el · Puan ${game.scores.getOrElse(0) { 0 }}"
                        } else {
                            "${game.playerName} · ${game.tricksWon.getOrElse(0) { 0 }} el · Puan ${game.scores.getOrElse(0) { 0 }}"
                        },
                        fontSize = 11,
                        background = Color.Black.copy(alpha = 0.4f)
                    )
                    val myTurnNow = game.phase == GamePhase.PLAYING && game.currentPlayer == 0
                    Spacer(Modifier.weight(1f))
                    if (myTurnNow) {
                        InfoChip(
                            text = "SIRA SENDE",
                            fontSize = 11,
                            background = BatakColors.Gold.copy(alpha = 0.9f),
                            textColor = Color(0xFF3A2A06)
                        )
                        Spacer(Modifier.weight(1f))
                    }
                    val myBid = game.bids.getOrNull(0)
                    InfoChip(
                        text = when {
                            myBid == null -> "İhale: —"
                            myBid == 0 -> "İhale: Pas"
                            else -> "İhale: $myBid"
                        },
                        fontSize = 11,
                        background = if ((myBid ?: 0) > 0) BatakColors.Gold.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.4f),
                        textColor = if ((myBid ?: 0) > 0) Color(0xFF3A2A06) else BatakColors.Cream
                    )
                }

                HandFan(
                    cards = game.hands.getOrElse(0) { emptyList() },
                    selected = selected,
                    myTurn = game.phase == GamePhase.PLAYING && game.currentPlayer == 0,
                    legalSet = legalSet,
                    cardW = handCardW,
                    cardH = handCardH,
                    design = settings.cardDesign,
                    animations = animated,
                    dealProgress = dealProgress.value,
                    onTap = { card ->
                        when {
                            game.phase != GamePhase.PLAYING || game.currentPlayer != 0 -> Unit
                            card !in legalSet -> vm.invalidCardFeedback(card)
                            selected == card -> {
                                selected = null
                                vm.humanPlayCard(card)
                            }
                            else -> selected = card
                        }
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // ---------------------------------------------------- kart oyna butonu
            if (game.phase == GamePhase.PLAYING && game.currentPlayer == 0) {
                val enabled = selected != null
                val buttonAlpha by animateFloatAsState(if (enabled) 1f else 0.45f, label = "playAlpha")
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 14.dp)
                        .offset(y = -(handCardH + 96.dp))
                        .graphicsLayer { alpha = buttonAlpha }
                        .shadow(8.dp, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                listOf(BatakColors.Gold, BatakColors.GoldDark)
                            )
                        )
                        .clickable(enabled = enabled) {
                            val card = selected
                            if (card != null) {
                                selected = null
                                vm.humanPlayCard(card)
                            }
                        }
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "KARTI OYNA",
                        color = Color(0xFF2B1F05),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // ------------------------------------------------------------- katmanlar
            ToastPill(
                message = toast,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (compact) 42.dp else 50.dp)
            )

            if (game.phase == GamePhase.ROUND_OVER && game.roundResult != null) {
                RoundResultOverlay(
                    game = game,
                    nameOf = { vm.nameOf(it) },
                    onContinue = { vm.continueAfterRound() }
                )
            }

            if (game.phase == GamePhase.GAME_OVER) {
                GameOverOverlay(
                    game = game,
                    nameOf = { vm.nameOf(it) },
                    onMenu = onExit,
                    onNewGame = {
                        vm.playClick()
                        vm.startNewMatch()
                    }
                )
            }
        }
    }
}

private fun buildBidStatus(game: BatakGame, vm: GameViewModel): String {
    val waiting = game.bids.getOrNull(game.bidTurn)
    return if (waiting == null) "${vm.nameOf(game.bidTurn)} düşünüyor..." else "İhale turu sürüyor..."
}

private fun alignmentFor(player: Int): Alignment = when (player) {
    0 -> Alignment.BottomCenter
    1 -> Alignment.CenterStart
    2 -> Alignment.TopCenter
    else -> Alignment.CenterEnd
}

private fun anchorVector(player: Int, width: Dp, height: Dp): Offset {
    val x = width.value * 0.45f
    val y = height.value * 0.32f
    return when (player) {
        0 -> Offset(0f, y)
        1 -> Offset(-x, 0f)
        2 -> Offset(0f, -y)
        else -> Offset(x, 0f)
    }
}

@Composable
private fun MenuPill(onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "MENÜ",
            color = BatakColors.Cream,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StatusPill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.42f))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = BatakColors.Cream, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SeatView(
    playerIndex: Int,
    game: BatakGame,
    vm: GameViewModel,
    miniCardW: Dp,
    miniCardH: Dp,
    design: CardDesign,
    dealProgress: Float,
    animated: Boolean,
    modifier: Modifier = Modifier
) {
    val name = vm.nameOf(playerIndex)
    val isTurn = game.phase in listOf(GamePhase.BIDDING, GamePhase.TRUMP_SELECTION, GamePhase.PLAYING) &&
        (game.phase == GamePhase.PLAYING && game.currentPlayer == playerIndex ||
            game.phase == GamePhase.BIDDING && game.bidTurn == playerIndex ||
            game.phase == GamePhase.TRUMP_SELECTION && game.highestBidder == playerIndex)
    val isWinner = game.phase == GamePhase.TRICK_RESOLUTION && game.lastTrickWinner == playerIndex
    val bid = game.bids.getOrNull(playerIndex)
    val tricks = game.tricksWon.getOrElse(playerIndex) { 0 }
    val score = game.scores.getOrElse(playerIndex) { 0 }
    val cardsLeft = game.hands.getOrElse(playerIndex) { emptyList() }.size
    val avatarSize = miniCardW * 2.4f

    val infinite = rememberInfiniteTransition(label = "turn")
    val pulseRaw by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "pulse"
    )
    val pulse = if (isTurn || isWinner) pulseRaw else 0f

    Column(
        modifier = modifier.width((miniCardW * 7f).coerceAtLeast(96.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(avatarSize)
                .shadow(if (isTurn) 8.dp else 2.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            if (isWinner) BatakColors.Gold.copy(alpha = 0.95f) else Color(0xFF1B3A2C),
                            Color(0xFF0A2018)
                        )
                    )
                )
                .border(
                    width = if (isTurn || isWinner) 2.dp else 1.dp,
                    color = when {
                        isTurn || isWinner -> BatakColors.Gold.copy(alpha = 0.4f + 0.6f * pulse)
                        game.mode == GameMode.PARTNERED && BatakRules.isPartner(playerIndex, 0) -> Color(0xFF4FC08D).copy(alpha = 0.85f)
                        game.mode == GameMode.PARTNERED -> Color(0xFFD96A5A).copy(alpha = 0.65f)
                        else -> Color.White.copy(alpha = 0.25f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(Locale.forLanguageTag("tr")),
                color = if (isWinner) Color(0xFF2B1F05) else BatakColors.Cream,
                fontSize = (miniCardW.value * 0.9f).sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = name,
            color = BatakColors.Cream,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Spacer(Modifier.height(3.dp))
        if (game.mode == GameMode.PARTNERED && BatakRules.isPartner(playerIndex, 0)) {
            InfoChip(
                text = "EŞİN",
                fontSize = 10,
                horizontalPadding = 8.dp,
                verticalPadding = 3.dp,
                background = Color(0xFF1F6B4A).copy(alpha = 0.95f)
            )
            Spacer(Modifier.height(3.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            val bidText = when {
                bid == null -> "—"
                bid == 0 -> "Pas"
                else -> "İhale $bid"
            }
            InfoChip(
                text = bidText,
                fontSize = 10,
                horizontalPadding = 7.dp,
                verticalPadding = 3.dp,
                background = when {
                    bid == null -> Color.Black.copy(alpha = 0.38f)
                    bid == 0 -> Color(0xFF5A2A2A).copy(alpha = 0.85f)
                    else -> BatakColors.Gold.copy(alpha = 0.9f)
                },
                textColor = if ((bid ?: 0) > 0) Color(0xFF3A2A06) else BatakColors.Cream
            )
            InfoChip(
                text = "$tricks el",
                fontSize = 10,
                horizontalPadding = 7.dp,
                verticalPadding = 3.dp
            )
            InfoChip(
                text = "$score p",
                fontSize = 10,
                horizontalPadding = 7.dp,
                verticalPadding = 3.dp,
                background = when {
                    score > 0 -> Color(0xFF1E5C3A).copy(alpha = 0.9f)
                    score < 0 -> Color(0xFF6B2323).copy(alpha = 0.9f)
                    else -> Color.Black.copy(alpha = 0.38f)
                }
            )
        }
        if (cardsLeft > 0) {
            Spacer(Modifier.height(4.dp))
            val step = miniCardW * 0.32f
            Box(
                Modifier
                    .height(miniCardH)
                    .width(miniCardW + step * (cardsLeft - 1).coerceAtLeast(0))
            ) {
                repeat(cardsLeft) { index ->
                    val t = if (animated) ((dealProgress * 15f) - index * 1.05f).coerceIn(0f, 1f) else 1f
                    Box(
                        Modifier
                            .offset(x = step * index)
                            .graphicsLayer { alpha = 0.35f + 0.65f * t }
                    ) {
                        CardBack(miniCardW, miniCardH, design)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrickCardView(
    play: PlayedCard,
    anchor: Offset,
    cardW: Dp,
    cardH: Dp,
    design: CardDesign,
    isWinner: Boolean,
    winnerPhase: Boolean,
    animations: Boolean,
    modifier: Modifier = Modifier
) {
    val anim = remember { Animatable(if (animations) 0f else 1f) }
    LaunchedEffect(play) {
        if (animations) {
            anim.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
        } else {
            anim.snapTo(1f)
        }
    }
    val winnerScale by animateFloatAsState(
        targetValue = if (isWinner && winnerPhase) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 350f),
        label = "winnerScale"
    )
    Box(
        modifier.graphicsLayer {
            val t = anim.value
            translationX = anchor.x * (1f - t)
            translationY = anchor.y * (1f - t)
            alpha = t
            val s = (0.82f + 0.18f * t) * winnerScale
            scaleX = s
            scaleY = s
        }
    ) {
        CardFace(
            card = play.card,
            width = cardW,
            height = cardH,
            design = design,
            highlighted = isWinner && winnerPhase
        )
    }
}

@Composable
private fun HandFan(
    cards: List<PlayingCard>,
    selected: PlayingCard?,
    myTurn: Boolean,
    legalSet: Set<PlayingCard>,
    cardW: Dp,
    cardH: Dp,
    design: CardDesign,
    animations: Boolean,
    dealProgress: Float,
    onTap: (PlayingCard) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(cardH + 30.dp)
    ) {
        val sortedCards = remember(cards) { BatakRules.sortHand(cards) }
        val count = sortedCards.size
        if (count == 0) return@BoxWithConstraints
        val density = LocalDensity.current
        val available = maxWidth - 20.dp
        val step = if (count <= 1) 0.dp else ((available - cardW) / (count - 1)).coerceAtLeast(8.dp)

        sortedCards.forEachIndexed { index, card ->
            val isSelected = card == selected
            val lift by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 0.dp,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 600f),
                label = "lift"
            )
            val t = if (animations) ((dealProgress * 15f) - index * 1.05f).coerceIn(0f, 1f) else 1f
            val clickWidth = if (index == count - 1) cardW else step
            val dim = myTurn && card !in legalSet

            Box(
                Modifier
                    .offset(x = 10.dp + step * index, y = 26.dp - lift)
                    .width(clickWidth.coerceAtLeast(10.dp))
                    .height(cardH + 26.dp)
                    .zIndex(if (isSelected) 100f else index.toFloat())
                    .graphicsLayer {
                        alpha = t
                        translationY = (1f - t) * -with(density) { cardH.toPx() } * 1.8f
                        val s = 0.9f + 0.1f * t
                        scaleX = s
                        scaleY = s
                    }
                    .clickable { onTap(card) }
            ) {
                CardFace(
                    card = card,
                    width = cardW,
                    height = cardH,
                    design = design,
                    highlighted = isSelected,
                    modifier = Modifier.alpha(if (dim) 0.45f else 1f)
                )
            }
        }
    }
}
