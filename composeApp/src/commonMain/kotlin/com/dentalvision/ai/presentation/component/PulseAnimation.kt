package com.dentalvision.ai.presentation.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    color: Color = Color.Red
) {
    val infiniteTransition = rememberInfiniteTransition()

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawPulseEffect(color, pulseScale, pulseAlpha)
    }
}

private fun DrawScope.drawPulseEffect(color: Color, scale: Float, alpha: Float) {
    val centerX = size.width / 2
    val centerY = size.height / 2

    // Outer pulse ring
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha * 0.3f),
                color.copy(alpha = 0f)
            ),
            center = Offset(centerX, centerY),
            radius = size.minDimension * scale * 0.5f
        ),
        radius = size.minDimension * scale * 0.5f,
        center = Offset(centerX, centerY)
    )

    // Middle pulse ring
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha * 0.5f),
                color.copy(alpha = 0f)
            ),
            center = Offset(centerX, centerY),
            radius = size.minDimension * scale * 0.35f
        ),
        radius = size.minDimension * scale * 0.35f,
        center = Offset(centerX, centerY)
    )

    // Inner pulse core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha),
                color.copy(alpha = alpha * 0.5f)
            ),
            center = Offset(centerX, centerY),
            radius = size.minDimension * scale * 0.2f
        ),
        radius = size.minDimension * scale * 0.2f,
        center = Offset(centerX, centerY)
    )
}

@Composable
fun AmberGlowAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawAmberGlow(glowIntensity)
    }
}

private fun DrawScope.drawAmberGlow(intensity: Float) {
    val amberColor = Color(0xFFFFC107)
    val centerX = size.width / 2
    val centerY = size.height / 2

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                amberColor.copy(alpha = intensity * 0.4f),
                amberColor.copy(alpha = intensity * 0.2f),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = size.minDimension * 0.6f
        ),
        radius = size.minDimension * 0.6f,
        center = Offset(centerX, centerY)
    )
}
