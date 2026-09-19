package com.batak.turkce.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batak.turkce.ui.theme.BatakColors

@Composable
fun TableBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(BatakColors.WoodDark, BatakColors.WoodLight, BatakColors.WoodDark)))
            }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    val radius = maxOf(w, h) * 0.8f
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(BatakColors.FeltLight, BatakColors.Felt, BatakColors.FeltDeep),
                            center = Offset(w / 2f, h * 0.42f),
                            radius = radius
                        ),
                        cornerRadius = CornerRadius(38f, 38f)
                    )
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                            center = Offset(w / 2f, h * 0.45f),
                            radius = radius * 1.05f
                        ),
                        cornerRadius = CornerRadius(38f, 38f)
                    )
                }
        )
        content()
    }
}

@Composable
fun InfoChip(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = Color.Black.copy(alpha = 0.35f),
    textColor: Color = BatakColors.Cream,
    fontSize: Int = 12,
    horizontalPadding: Dp = 10.dp,
    verticalPadding: Dp = 5.dp
) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}
