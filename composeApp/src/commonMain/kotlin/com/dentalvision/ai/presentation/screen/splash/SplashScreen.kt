package com.dentalvision.ai.presentation.screen.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * SplashScreen - Professional animated welcome screen for DentalVision AI
 *
 * Features:
 * - Vertical gradient background (Deep Blue to Light Blue)
 * - Logo bounce animation with spring overshoot effect
 * - Text fade-in with slide-up animation
 * - Floating geometric shapes for depth and parallax effect
 * - 3-second display duration before navigating to login
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit
) {
    var logoVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        logoVisible = true
        delay(300)
        textVisible = true
        delay(3000)
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A), // Deep Blue
                        Color(0xFF3B82F6)  // Light Blue
                    )
                )
            )
    ) {
        FloatingShapesBackground()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedLogo(visible = logoVisible)

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedTitle(visible = textVisible)

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedSubtitle(visible = textVisible)
        }
    }
}

/**
 * Logo animation with spring bounce effect
 */
@Composable
private fun AnimatedLogo(visible: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    Icon(
        imageVector = Icons.Filled.Favorite,
        contentDescription = "DentalVision AI Logo",
        modifier = Modifier
            .size(120.dp)
            .scale(scale),
        tint = Color.White
    )
}

/**
 * Title text with fade-in and slide-up animation
 */
@Composable
private fun AnimatedTitle(visible: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "title_alpha"
    )

    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 50.dp,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "title_offset"
    )

    Text(
        text = "DentalVision AI",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        ),
        color = Color.White,
        modifier = Modifier
            .offset(y = offsetY)
            .alpha(alpha)
    )
}

/**
 * Subtitle text with fade-in and slide-up animation
 */
@Composable
private fun AnimatedSubtitle(visible: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "subtitle_alpha"
    )

    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 30.dp,
        animationSpec = tween(durationMillis = 1000, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "subtitle_offset"
    )

    Text(
        text = "Advanced Dental Analysis System",
        style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        ),
        color = Color.White.copy(alpha = 0.9f),
        modifier = Modifier
            .offset(y = offsetY)
            .alpha(alpha)
    )
}

/**
 * Floating geometric shapes in the background for parallax effect
 * Creates subtle depth and professional ambiance
 */
@Composable
private fun FloatingShapesBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "floating_shapes")

    val shape1OffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shape1_offset_y"
    )

    val shape2OffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shape2_offset_y"
    )

    val shape3OffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shape3_offset_y"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingCircle(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 40.dp, y = 100.dp + shape1OffsetY.dp),
            size = 80.dp,
            alpha = 0.15f
        )

        FloatingCircle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-60).dp, y = 200.dp + shape2OffsetY.dp),
            size = 120.dp,
            alpha = 0.1f
        )

        FloatingCircle(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 80.dp, y = (-150).dp + shape3OffsetY.dp),
            size = 100.dp,
            alpha = 0.12f
        )

        FloatingCircle(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-40).dp, y = (-100).dp + shape1OffsetY.dp),
            size = 90.dp,
            alpha = 0.08f
        )
    }
}

/**
 * Individual floating circle shape
 */
@Composable
private fun FloatingCircle(
    modifier: Modifier = Modifier,
    size: Dp,
    alpha: Float
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = Color.White.copy(alpha = alpha),
                shape = CircleShape
            )
    )
}
