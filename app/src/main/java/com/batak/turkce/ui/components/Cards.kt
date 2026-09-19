package com.batak.turkce.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.batak.turkce.model.CardDesign
import com.batak.turkce.model.PlayingCard
import com.batak.turkce.model.Rank
import com.batak.turkce.model.Suit
import com.batak.turkce.ui.theme.BatakColors

@Composable
fun CardFace(
    card: PlayingCard,
    width: Dp,
    height: Dp,
    design: CardDesign = CardDesign.KLASIK,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val faceTop = if (design == CardDesign.KLASIK) Color(0xFFFEFCF6) else Color(0xFFF8F2E4)
    val faceBottom = if (design == CardDesign.KLASIK) Color(0xFFEFE8D8) else Color(0xFFE6DCC4)
    val ink = if (card.suit.isRed) BatakColors.CardRed else BatakColors.CardBlack
    val shape = RoundedCornerShape(width * 0.13f)
    val cornerRank = with(density) { (width.toPx() * 0.22f).toSp() }
    val cornerSuitSize = width * 0.14f
    val aceSize = width * 0.44f
    val pipSize = width * 0.36f
    val courtRank = with(density) { (width.toPx() * 0.24f).toSp() }
    val courtSuitSize = width * 0.20f

    Box(
        modifier
            .size(width, height)
            .shadow(if (highlighted) 10.dp else 2.dp, shape, clip = false)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(faceTop, faceBottom)))
            .border(
                if (highlighted) 2.dp else 1.dp,
                if (highlighted) BatakColors.Gold else Color(0x26000000),
                shape
            )
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                card.rank == Rank.ACE -> {
                    SuitSymbol(card.suit, aceSize, ink)
                }
                card.rank.value in Rank.JACK.value..Rank.KING.value -> {
                    Text(
                        text = card.rank.label,
                        color = ink,
                        fontSize = courtRank,
                        lineHeight = courtRank,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(width * 0.03f))
                    SuitSymbol(card.suit, courtSuitSize, ink)
                }
                else -> {
                    SuitSymbol(card.suit, pipSize, ink)
                }
            }
        }

        CardCorner(
            rank = card.rank.label,
            suit = card.suit,
            color = ink,
            rankSize = cornerRank,
            suitSize = cornerSuitSize,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = width * 0.06f, top = width * 0.04f)
        )

        CardCorner(
            rank = card.rank.label,
            suit = card.suit,
            color = ink,
            rankSize = cornerRank,
            suitSize = cornerSuitSize,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = width * 0.06f, bottom = width * 0.04f)
                .rotate(180f)
        )
    }
}

@Composable
private fun CardCorner(
    rank: String,
    suit: Suit,
    color: Color,
    rankSize: androidx.compose.ui.unit.TextUnit,
    suitSize: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = rank,
            color = color,
            fontSize = rankSize,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            lineHeight = rankSize
        )
        Spacer(Modifier.height(suitSize * 0.08f))
        SuitSymbol(suit, suitSize, color)
    }
}

@Composable
fun CardBack(
    width: Dp,
    height: Dp,
    design: CardDesign = CardDesign.KLASIK,
    modifier: Modifier = Modifier
) {
    val base = if (design == CardDesign.KLASIK) Color(0xFF8E2233) else Color(0xFF1B3A5C)
    val baseDark = if (design == CardDesign.KLASIK) Color(0xFF661522) else Color(0xFF102641)
    val accent = if (design == CardDesign.KLASIK) Color(0xFFE3C26A) else Color(0xFFA9CCE8)
    val shape = RoundedCornerShape(width * 0.13f)
    val glyph = with(LocalDensity.current) { (width.toPx() * 0.36f).toSp() }

    Box(
        modifier
            .size(width, height)
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(Brush.linearGradient(listOf(base, baseDark)))
            .border(1.5.dp, accent.copy(alpha = 0.5f), shape)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(width * 0.09f)
        ) {
            val w = size.width
            val h = size.height
            val step = maxOf(w, h) / 4.2f
            var x = -h
            while (x < w) {
                drawLine(
                    color = accent.copy(alpha = 0.22f),
                    start = Offset(x, 0f),
                    end = Offset(x + h, h),
                    strokeWidth = 1.1f
                )
                drawLine(
                    color = accent.copy(alpha = 0.12f),
                    start = Offset(x + h, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.1f
                )
                x += step
            }
            drawRoundRect(
                color = accent.copy(alpha = 0.28f),
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(w * 0.09f),
                style = Stroke(width = 1.4f)
            )
        }
        Text(
            text = "B",
            color = accent.copy(alpha = 0.85f),
            fontSize = glyph,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
