package com.batak.turkce.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batak.turkce.model.CardDesign
import com.batak.turkce.model.PlayingCard
import com.batak.turkce.model.Rank
import com.batak.turkce.model.Suit
import com.batak.turkce.ui.components.CardFace
import com.batak.turkce.ui.components.SuitSymbol
import com.batak.turkce.ui.components.TableBackground
import com.batak.turkce.ui.theme.BatakColors

@Composable
fun MainMenuScreen(
    hasSave: Boolean,
    playerName: String,
    cardDesign: CardDesign,
    onContinue: () -> Unit,
    onNewGame: () -> Unit,
    onSettings: () -> Unit,
    onHowTo: () -> Unit,
    onStats: () -> Unit,
    onAbout: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "menu")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )

    TableBackground {
        BackgroundCards(cardDesign, drift)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "BATAK",
                color = BatakColors.Gold,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 10.sp,
                modifier = Modifier
                    .shadow(14.dp)
                    .graphicsLayer { translationY = -drift * 4f }
            )
            Text(
                text = "TÜRKÇE İHALELİ BATAK",
                color = BatakColors.Cream.copy(alpha = 0.85f),
                fontSize = 12.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Suit.entries.forEach { suit ->
                    SuitSymbol(
                        suit = suit,
                        size = 22.dp,
                        color = if (suit.isRed) Color(0xFFE2635A) else BatakColors.Cream
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Hoş geldin, $playerName",
                color = BatakColors.Cream.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(30.dp))

            if (hasSave) {
                MenuButton(
                    text = "DEVAM ET",
                    primary = true,
                    onClick = onContinue
                )
                Spacer(Modifier.height(10.dp))
                MenuButton(
                    text = "YENİ OYUN",
                    primary = false,
                    onClick = onNewGame
                )
            } else {
                MenuButton(
                    text = "OYUNA BAŞLA",
                    primary = true,
                    onClick = onNewGame
                )
            }

            Spacer(Modifier.height(10.dp))
            MenuButton(text = "OYUN AYARLARI", primary = false, onClick = onSettings)
            Spacer(Modifier.height(10.dp))
            MenuButton(text = "NASIL OYNANIR?", primary = false, onClick = onHowTo)
            Spacer(Modifier.height(10.dp))
            MenuButton(text = "İSTATİSTİKLER", primary = false, onClick = onStats)
            Spacer(Modifier.height(10.dp))
            MenuButton(text = "HAKKINDA", primary = false, onClick = onAbout)

            Spacer(Modifier.height(26.dp))
            Text(
                text = "Sürüm 1.1.0  ·  Çevrimdışı oynanır",
                color = BatakColors.Cream.copy(alpha = 0.45f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BoxScope.BackgroundCards(design: CardDesign, drift: Float) {
    CardFace(
        card = PlayingCard(Suit.SPADES, Rank.ACE),
        width = 74.dp,
        height = 106.dp,
        design = design,
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = 8.dp, y = 120.dp)
            .graphicsLayer {
                rotationZ = -20f + drift * 8f
                translationY = drift * 18f
            }
            .alpha(0.22f)
    )
    CardFace(
        card = PlayingCard(Suit.HEARTS, Rank.KING),
        width = 68.dp,
        height = 98.dp,
        design = design,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .offset(x = 26.dp, y = -40.dp)
            .graphicsLayer {
                rotationZ = 16f - drift * 6f
                translationY = -drift * 22f
            }
            .alpha(0.18f)
    )
    CardFace(
        card = PlayingCard(Suit.DIAMONDS, Rank.QUEEN),
        width = 70.dp,
        height = 100.dp,
        design = design,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .offset(x = 14.dp, y = -60.dp)
            .graphicsLayer {
                rotationZ = 14f + drift * 5f
                translationY = drift * 14f
            }
            .alpha(0.16f)
    )
}

@Composable
fun MenuButton(
    text: String,
    primary: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val background = if (primary) {
        Brush.verticalGradient(listOf(BatakColors.Gold, BatakColors.GoldDark))
    } else {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.13f), Color.White.copy(alpha = 0.06f)))
    }
    Box(
        modifier
            .fillMaxWidth()
            .height(if (primary) 58.dp else 50.dp)
            .shadow(if (primary) 12.dp else 4.dp, shape)
            .clip(shape)
            .background(background)
            .border(
                width = 1.dp,
                color = if (primary) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.18f),
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (primary) Color(0xFF2B1F05) else BatakColors.Cream,
            fontSize = if (primary) 16.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}
