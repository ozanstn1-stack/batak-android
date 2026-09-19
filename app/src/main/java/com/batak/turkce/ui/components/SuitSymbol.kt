package com.batak.turkce.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import com.batak.turkce.model.Suit

@Composable
fun SuitSymbol(
    suit: Suit,
    size: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        when (suit) {
            Suit.HEARTS -> drawPath(heartPath(w, h), color)
            Suit.DIAMONDS -> drawPath(diamondPath(w, h), color)
            Suit.SPADES -> drawPath(spadePath(w, h), color)
            Suit.CLUBS -> {
                drawPath(clubStemPath(w, h), color)
                val radius = w * 0.24f
                drawCircle(color, radius, Offset(w * 0.5f, h * 0.27f))
                drawCircle(color, radius, Offset(w * 0.27f, h * 0.58f))
                drawCircle(color, radius, Offset(w * 0.73f, h * 0.58f))
            }
        }
    }
}

private fun heartPath(w: Float, h: Float): Path = Path().apply {
    moveTo(w * 0.5f, h * 0.30f)
    cubicTo(w * 0.42f, h * 0.12f, w * 0.20f, h * 0.08f, w * 0.10f, h * 0.28f)
    cubicTo(w * 0.0f, h * 0.48f, w * 0.18f, h * 0.66f, w * 0.5f, h * 0.94f)
    cubicTo(w * 0.82f, h * 0.66f, w * 1.0f, h * 0.48f, w * 0.90f, h * 0.28f)
    cubicTo(w * 0.80f, h * 0.08f, w * 0.58f, h * 0.12f, w * 0.5f, h * 0.30f)
    close()
}

private fun diamondPath(w: Float, h: Float): Path = Path().apply {
    moveTo(w * 0.5f, h * 0.02f)
    lineTo(w * 0.96f, h * 0.5f)
    lineTo(w * 0.5f, h * 0.98f)
    lineTo(w * 0.04f, h * 0.5f)
    close()
}

private fun spadePath(w: Float, h: Float): Path = Path().apply {
    moveTo(w * 0.5f, h * 0.0f)
    cubicTo(w * 0.36f, h * 0.24f, w * 0.05f, h * 0.38f, w * 0.05f, h * 0.60f)
    cubicTo(w * 0.05f, h * 0.76f, w * 0.18f, h * 0.85f, w * 0.30f, h * 0.80f)
    cubicTo(w * 0.35f, h * 0.78f, w * 0.39f, h * 0.75f, w * 0.42f, h * 0.70f)
    lineTo(w * 0.36f, h * 0.98f)
    lineTo(w * 0.64f, h * 0.98f)
    lineTo(w * 0.58f, h * 0.70f)
    cubicTo(w * 0.61f, h * 0.75f, w * 0.65f, h * 0.78f, w * 0.70f, h * 0.80f)
    cubicTo(w * 0.82f, h * 0.85f, w * 0.95f, h * 0.76f, w * 0.95f, h * 0.60f)
    cubicTo(w * 0.95f, h * 0.38f, w * 0.64f, h * 0.24f, w * 0.5f, h * 0.0f)
    close()
}

private fun clubStemPath(w: Float, h: Float): Path = Path().apply {
    moveTo(w * 0.44f, h * 0.55f)
    lineTo(w * 0.56f, h * 0.55f)
    lineTo(w * 0.70f, h * 1.0f)
    lineTo(w * 0.30f, h * 1.0f)
    close()
}
