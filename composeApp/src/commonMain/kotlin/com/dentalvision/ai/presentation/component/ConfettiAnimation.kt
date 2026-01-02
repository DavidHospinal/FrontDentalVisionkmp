package com.dentalvision.ai.presentation.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    particleCount: Int = 50
) {
    val infiniteTransition = rememberInfiniteTransition()

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val particles = remember {
        List(particleCount) {
            ConfettiParticle(
                startX = Random.nextFloat(),
                startY = -0.1f,
                color = listOf(
                    Color(0xFF4CAF50),  // Green
                    Color(0xFF00BCD4),  // Teal
                    Color(0xFFFFEB3B),  // Yellow
                    Color(0xFFFF9800),  // Orange
                    Color(0xFFE91E63)   // Pink
                ).random(),
                shape = if (Random.nextBoolean()) ParticleShape.CIRCLE else ParticleShape.RECTANGLE,
                rotationSpeed = Random.nextFloat() * 360f,
                wobbleAmplitude = Random.nextFloat() * 0.1f,
                fallSpeed = 0.5f + Random.nextFloat() * 0.5f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawConfettiParticle(particle, animationProgress)
        }
    }
}

private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val color: Color,
    val shape: ParticleShape,
    val rotationSpeed: Float,
    val wobbleAmplitude: Float,
    val fallSpeed: Float
)

private enum class ParticleShape {
    CIRCLE, RECTANGLE
}

private fun DrawScope.drawConfettiParticle(particle: ConfettiParticle, progress: Float) {
    val screenHeight = size.height
    val screenWidth = size.width

    val y = particle.startY * screenHeight + (progress * screenHeight * particle.fallSpeed)
    val wobbleX = sin(progress * 6.28f * 2) * particle.wobbleAmplitude * screenWidth
    val x = particle.startX * screenWidth + wobbleX

    if (y > screenHeight) return

    val rotation = progress * particle.rotationSpeed

    val particleSize = 12f

    rotate(rotation, pivot = Offset(x, y)) {
        when (particle.shape) {
            ParticleShape.CIRCLE -> {
                drawCircle(
                    color = particle.color,
                    radius = particleSize / 2,
                    center = Offset(x, y)
                )
            }
            ParticleShape.RECTANGLE -> {
                drawRect(
                    color = particle.color,
                    topLeft = Offset(x - particleSize / 2, y - particleSize / 2),
                    size = androidx.compose.ui.geometry.Size(particleSize, particleSize * 1.5f)
                )
            }
        }
    }
}

@Composable
fun SerpentineAnimation(
    modifier: Modifier = Modifier,
    streamCount: Int = 8
) {
    val infiniteTransition = rememberInfiniteTransition()

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val streams = remember {
        List(streamCount) {
            SerpentineStream(
                startX = Random.nextFloat(),
                color = listOf(
                    Color(0xFF4CAF50),
                    Color(0xFF00BCD4),
                    Color(0xFFFFEB3B)
                ).random(),
                waveFrequency = 2f + Random.nextFloat() * 2f,
                waveAmplitude = 0.05f + Random.nextFloat() * 0.05f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        streams.forEach { stream ->
            drawSerpentineStream(stream, animationProgress)
        }
    }
}

private data class SerpentineStream(
    val startX: Float,
    val color: Color,
    val waveFrequency: Float,
    val waveAmplitude: Float
)

private fun DrawScope.drawSerpentineStream(stream: SerpentineStream, progress: Float) {
    val screenHeight = size.height
    val screenWidth = size.width

    val path = Path()
    val segments = 50
    val yProgress = progress * screenHeight

    for (i in 0 until segments) {
        val t = i.toFloat() / segments
        val y = t * yProgress
        val waveX = sin(t * 6.28f * stream.waveFrequency + progress * 6.28f) * stream.waveAmplitude * screenWidth
        val x = stream.startX * screenWidth + waveX

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = stream.color.copy(alpha = 0.7f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
    )
}
