package com.dentalvision.ai.presentation.screen.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import dentalvisionai.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_animation")

    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    val animatedGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E3A8A),
            Color(0xFF3B82F6),
            Color(0xFF60A5FA),
            Color(0xFF3B82F6),
            Color(0xFF1E3A8A)
        ),
        startY = gradientOffset,
        endY = gradientOffset + 1000f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = animatedGradient)
            .clickable { onNavigateToLogin() }
    ) {
        FloatingShapesBackground()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedGifLogo()

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedTitle()

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedSubtitle()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Toca en cualquier lugar para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.alpha(0.8f)
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AnimatedGifLogo() {
    val scale by rememberInfiniteTransition(label = "logo_pulse").animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale_pulse"
    )

    val platformContext = LocalPlatformContext.current

    val gifBytes by produceState<ByteArray?>(initialValue = null) {
        value = withContext(Dispatchers.Default) {
            try {
                Res.readBytes("drawable/BeeClean.gif")
            } catch (e: Exception) {
                null
            }
        }
    }

    Box(
        modifier = Modifier
            .size(200.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            gifBytes != null -> {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(platformContext)
                        .data(gifBytes)
                        .build(),
                    contentDescription = "DentalVision AI Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    },
                    error = { error ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "DV",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "GIF decode error",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = error.result.throwable?.message ?: "Unknown",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                )
            }
            else -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedTitle() {
    val alpha by rememberInfiniteTransition(label = "title_glow").animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "title_alpha_glow"
    )

    Text(
        text = "DentalVision AI",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        ),
        color = Color.White,
        modifier = Modifier.alpha(alpha)
    )
}

@Composable
private fun AnimatedSubtitle() {
    val alpha by rememberInfiniteTransition(label = "subtitle_glow").animateFloat(
        initialValue = 0.7f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "subtitle_alpha_glow"
    )

    Text(
        text = "Advanced Dental Analysis System",
        style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        ),
        color = Color.White.copy(alpha = alpha)
    )
}

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
