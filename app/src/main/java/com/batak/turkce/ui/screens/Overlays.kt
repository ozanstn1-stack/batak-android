package com.batak.turkce.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batak.turkce.engine.BatakGame
import com.batak.turkce.engine.BatakRules
import com.batak.turkce.model.GameMode
import com.batak.turkce.model.Suit
import com.batak.turkce.ui.components.SuitSymbol
import com.batak.turkce.ui.theme.BatakColors

@Composable
fun ToastPill(message: String?, modifier: Modifier = Modifier) {
    var lastMessage by remember { mutableStateOf("") }
    LaunchedEffect(message) {
        if (message != null) lastMessage = message
    }
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { -it / 2 },
        exit = fadeOut() + slideOutVertically { -it / 2 },
        modifier = modifier
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.78f))
                .border(1.dp, BatakColors.Gold.copy(alpha = 0.35f), RoundedCornerShape(50))
                .padding(horizontal = 18.dp, vertical = 9.dp)
        ) {
            Text(
                text = lastMessage,
                color = BatakColors.Cream,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BidPanel(
    highestBid: Int,
    highestBidderName: String?,
    minBid: Int,
    canBid: (Int) -> Boolean,
    onBid: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    val options = (minBid..BatakRules.MAX_BID).toList()
    val buttons = listOf(0) + options

    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.52f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "İHALE SIRASI SENDE",
                color = BatakColors.Gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = highestBidderName?.let { "En yüksek: $highestBid ($it)" } ?: "Henüz ihale yok",
                color = BatakColors.Cream.copy(alpha = 0.78f),
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.height(10.dp))
        buttons.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { value ->
                    if (value == 0) {
                        BidButton("PAS", enabled = true, isPass = true, modifier = Modifier.weight(1f)) { onBid(0) }
                    } else {
                        BidButton(
                            text = value.toString(),
                            enabled = canBid(value),
                            modifier = Modifier.weight(1f)
                        ) { onBid(value) }
                    }
                }
                repeat(4 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun BidButton(
    text: String,
    enabled: Boolean,
    isPass: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = when {
        !enabled -> Brush.verticalGradient(listOf(Color(0xFF3B3B3B), Color(0xFF2A2A2A)))
        isPass -> Brush.verticalGradient(listOf(Color(0xFF96423A), Color(0xFF61261F)))
        else -> Brush.verticalGradient(listOf(BatakColors.Gold, BatakColors.GoldDark))
    }
    val foreground = when {
        !enabled -> Color.White.copy(alpha = 0.32f)
        isPass -> BatakColors.Cream
        else -> Color(0xFF2B1F05)
    }
    Box(
        modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = foreground,
            fontWeight = FontWeight.Bold,
            fontSize = if (text.length > 2) 12.sp else 15.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun TrumpPanel(
    bid: Int,
    onPick: (Suit) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.52f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
            .padding(12.dp)
    ) {
        Text(
            text = "İHALEYİ ALDIN ($bid) — KOZU SEÇ",
            color = BatakColors.Gold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Suit.entries.forEach { suit ->
                Column(
                    Modifier
                        .weight(1f)
                        .height(74.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFDFBF4))
                        .clickable { onPick(suit) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SuitSymbol(
                        suit = suit,
                        size = 28.dp,
                        color = if (suit.isRed) BatakColors.CardRed else BatakColors.CardBlack
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = suit.labelTr,
                        fontSize = 11.sp,
                        color = Color(0xFF3A3A3A),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun RoundResultOverlay(
    game: BatakGame,
    nameOf: (Int) -> String,
    onContinue: () -> Unit
) {
    val result = game.roundResult ?: return
    val partnered = game.mode == GameMode.PARTNERED
    val contractTeam = if (game.highestBidder >= 0) BatakRules.teamOf(game.highestBidder) else -1
    val shape = RoundedCornerShape(26.dp)

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.66f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .fillMaxWidth(0.94f)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(Color(0xFF1A4433), Color(0xFF0A2418))))
                .border(1.5.dp, BatakColors.Gold.copy(alpha = 0.55f), shape)
                .padding(18.dp)
        ) {
            Text(
                text = "EL BİTTİ",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = BatakColors.Gold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Koz: ${result.trump.labelTr} ${result.trump.symbol}  ·  El ${game.roundNumber} / ${game.totalRounds}" +
                    if (partnered) "  ·  Eşli" else "  ·  Eşsiz",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = BatakColors.Cream.copy(alpha = 0.75f),
                fontSize = 12.sp
            )
            if (partnered && contractTeam >= 0) {
                Spacer(Modifier.height(6.dp))
                val teamLabel = if (contractTeam == BatakRules.teamOf(0)) "Senin takımın" else "Rakip takım"
                Text(
                    text = "İhale: ${game.highestBid} — $teamLabel (${teamPlayerNames(contractTeam, nameOf)})",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = if (teamLabel == "Senin takımın") BatakColors.Success else BatakColors.Danger,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                Text("OYUNCU", Modifier.weight(1f), color = BatakColors.Cream.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.sp)
                Text("İHALE", Modifier.width(52.dp), color = BatakColors.Cream.copy(alpha = 0.6f), fontSize = 10.sp, textAlign = TextAlign.Center)
                Text("ALINAN", Modifier.width(52.dp), color = BatakColors.Cream.copy(alpha = 0.6f), fontSize = 10.sp, textAlign = TextAlign.Center)
                Text("PUAN", Modifier.width(52.dp), color = BatakColors.Cream.copy(alpha = 0.6f), fontSize = 10.sp, textAlign = TextAlign.End)
            }
            Spacer(Modifier.height(6.dp))

            for (player in 0..3) {
                val bidText = when {
                    !partnered -> if (result.bids.getOrElse(player) { 0 } == 0) "Pas" else "${result.bids.getOrElse(player) { 0 }}"
                    contractTeam >= 0 && BatakRules.teamOf(player) == contractTeam -> "${game.highestBid}"
                    else -> "—"
                }
                ResultRow(
                    name = nameOf(player) + if (partnered && BatakRules.isPartner(player, 0)) " (eşin)" else "",
                    bidText = bidText,
                    tricks = result.tricksWon.getOrElse(player) { 0 },
                    score = result.scores.getOrElse(player) { 0 },
                    highlight = player == 0 || (partnered && BatakRules.isPartner(player, 0))
                )
            }

            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.12f)))
            Spacer(Modifier.height(10.dp))

            Text(
                text = if (partnered) "TAKIM TOPLAMLARI" else "TOPLAM PUAN",
                color = BatakColors.Cream.copy(alpha = 0.6f),
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            if (partnered) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TeamTotalCard(
                        label = "Senin Takımın",
                        names = teamPlayerNames(BatakRules.teamOf(0), nameOf),
                        score = game.scores.getOrElse(0) { 0 } + game.scores.getOrElse(2) { 0 },
                        tricks = game.cumulativeTricks.getOrElse(0) { 0 } + game.cumulativeTricks.getOrElse(2) { 0 },
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                    TeamTotalCard(
                        label = "Rakip Takım",
                        names = teamPlayerNames(BatakRules.teamOf(1), nameOf),
                        score = game.scores.getOrElse(1) { 0 } + game.scores.getOrElse(3) { 0 },
                        tricks = game.cumulativeTricks.getOrElse(1) { 0 } + game.cumulativeTricks.getOrElse(3) { 0 },
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (player in 0..3) {
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (player == 0) BatakColors.Gold.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.25f))
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = nameOf(player),
                                color = BatakColors.Cream.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "${game.scores.getOrElse(player) { 0 }}",
                                color = if (game.scores.getOrElse(player) { 0 } >= 0) BatakColors.Gold else BatakColors.Danger,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            GoldActionButton(
                text = if (game.roundNumber >= game.totalRounds) "SONUÇLAR" else "DEVAM",
                onClick = onContinue
            )
        }
    }
}

private fun teamPlayerNames(team: Int, nameOf: (Int) -> String): String {
    val members = (0..3).filter { BatakRules.teamOf(it) == team }
    return members.joinToString(" + ") { nameOf(it) }
}

@Composable
private fun TeamTotalCard(
    label: String,
    names: String,
    score: Int,
    tricks: Int,
    highlight: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (highlight) BatakColors.Gold.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.25f))
            .border(1.dp, if (highlight) BatakColors.Gold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Text(label, color = if (highlight) BatakColors.Gold else BatakColors.Cream.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(names, color = BatakColors.Cream.copy(alpha = 0.6f), fontSize = 10.sp, maxLines = 1)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$score",
                color = if (score >= 0) BatakColors.Gold else BatakColors.Danger,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(6.dp))
            Text("$tricks el", color = BatakColors.Cream.copy(alpha = 0.6f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun ResultRow(
    name: String,
    bidText: String,
    tricks: Int,
    score: Int,
    highlight: Boolean
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (highlight) BatakColors.Gold.copy(alpha = 0.14f) else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            color = if (highlight) BatakColors.Gold else BatakColors.Cream,
            fontSize = 14.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
        Text(
            text = bidText,
            modifier = Modifier.width(52.dp),
            color = BatakColors.Cream.copy(alpha = 0.85f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "$tricks",
            modifier = Modifier.width(52.dp),
            color = BatakColors.Cream.copy(alpha = 0.85f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        val scoreText = when {
            score > 0 -> "+$score"
            score < 0 -> "$score"
            else -> "0"
        }
        Text(
            text = scoreText,
            modifier = Modifier.width(52.dp),
            color = when {
                score > 0 -> BatakColors.Success
                score < 0 -> BatakColors.Danger
                else -> BatakColors.Cream.copy(alpha = 0.6f)
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun GameOverOverlay(
    game: BatakGame,
    nameOf: (Int) -> String,
    onMenu: () -> Unit,
    onNewGame: () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    val partnered = game.mode == GameMode.PARTNERED
    val humanWon: Boolean
    val headline: String

    if (partnered) {
        val myTeamScore = game.scores.getOrElse(0) { 0 } + game.scores.getOrElse(2) { 0 }
        val otherTeamScore = game.scores.getOrElse(1) { 0 } + game.scores.getOrElse(3) { 0 }
        humanWon = myTeamScore >= otherTeamScore
        headline = if (humanWon) "Takımın kazandı!" else "Kazanan: Rakip Takım"
    } else {
        val standings = (0..3).sortedByDescending { game.scores.getOrElse(it) { 0 } }
        humanWon = game.scores.getOrElse(0) { 0 } >= (1..3).maxOf { game.scores.getOrElse(it) { 0 } }
        headline = if (humanWon) "Kazandın!" else "Kazanan: ${nameOf(standings.first())}"
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .fillMaxWidth(0.94f)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(Color(0xFF1A4433), Color(0xFF081D13))))
                .border(1.5.dp, BatakColors.Gold.copy(alpha = 0.6f), shape)
                .padding(20.dp)
        ) {
            Text(
                text = "OYUN BİTTİ",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = BatakColors.Gold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = headline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = if (humanWon) BatakColors.Success else BatakColors.Cream,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (partnered) "Eşli (2v2)" else "Eşsiz (tek)",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = BatakColors.Cream.copy(alpha = 0.55f),
                fontSize = 11.sp
            )
            Spacer(Modifier.height(16.dp))

            if (partnered) {
                val teams = (0..1).sortedByDescending { team ->
                    game.scores.getOrElse(team) { 0 } + game.scores.getOrElse(team + 2) { 0 }
                }
                teams.forEachIndexed { index, team ->
                    val score = game.scores.getOrElse(team) { 0 } + game.scores.getOrElse(team + 2) { 0 }
                    val tricks = game.cumulativeTricks.getOrElse(team) { 0 } + game.cumulativeTricks.getOrElse(team + 2) { 0 }
                    val isHumanTeam = team == BatakRules.teamOf(0)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isHumanTeam) BatakColors.Gold.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.25f))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            modifier = Modifier.width(28.dp),
                            color = if (index == 0) BatakColors.Gold else BatakColors.Cream.copy(alpha = 0.6f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = if (isHumanTeam) "Senin Takımın" else "Rakip Takım",
                                color = if (isHumanTeam) BatakColors.Gold else BatakColors.Cream,
                                fontSize = 15.sp,
                                fontWeight = if (isHumanTeam) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = teamPlayerNames(team, nameOf),
                                color = BatakColors.Cream.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                        Text("$tricks el", color = BatakColors.Cream.copy(alpha = 0.6f), fontSize = 12.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "$score",
                            color = if (score >= 0) BatakColors.Gold else BatakColors.Danger,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
            } else {
                val standings = (0..3).sortedByDescending { game.scores.getOrElse(it) { 0 } }
                for ((index, player) in standings.withIndex()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (player == 0) BatakColors.Gold.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.25f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            modifier = Modifier.width(28.dp),
                            color = when (index) {
                                0 -> BatakColors.Gold
                                1 -> Color(0xFFC0C0C0)
                                2 -> Color(0xFFCD9666)
                                else -> BatakColors.Cream.copy(alpha = 0.6f)
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = nameOf(player),
                            modifier = Modifier.weight(1f),
                            color = if (player == 0) BatakColors.Gold else BatakColors.Cream,
                            fontSize = 15.sp,
                            fontWeight = if (player == 0) FontWeight.Bold else FontWeight.Medium
                        )
                        Text(
                            text = "${game.cumulativeTricks.getOrElse(player) { 0 }} el",
                            color = BatakColors.Cream.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "${game.scores.getOrElse(player) { 0 }}",
                            color = if (game.scores.getOrElse(player) { 0 } >= 0) BatakColors.Gold else BatakColors.Danger,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
                        .clickable { onMenu() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("ANA MENÜ", color = BatakColors.Cream, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Box(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.verticalGradient(listOf(BatakColors.Gold, BatakColors.GoldDark)))
                        .clickable { onNewGame() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("YENİ OYUN", color = Color(0xFF2B1F05), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun GoldActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(50))
            .background(Brush.verticalGradient(listOf(BatakColors.Gold, BatakColors.GoldDark)))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF2B1F05),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
    }
}
