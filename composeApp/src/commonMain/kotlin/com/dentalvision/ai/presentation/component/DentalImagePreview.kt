package com.dentalvision.ai.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.dentalvision.ai.presentation.theme.DentalColors
import io.github.aakira.napier.Napier

@Composable
fun DentalImagePreview(
    imageData: Any,
    contentDescription: String = "Dental image preview",
    showZoomControls: Boolean = true,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)
        offset += offsetChange
    }

    // Log para debugging
    LaunchedEffect(imageData) {
        when (imageData) {
            is ByteArray -> Napier.d("DentalImagePreview: Loading ByteArray image (${imageData.size} bytes)")
            is String -> Napier.d("DentalImagePreview: Loading URL image: $imageData")
            else -> Napier.w("DentalImagePreview: Unknown image data type: ${imageData::class.simpleName}")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Image with zoom/pan - Using Coil3 for cross-platform support
        val platformContext = LocalPlatformContext.current

        SubcomposeAsyncImage(
            model = ImageRequest.Builder(platformContext)
                .data(imageData)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformableState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = DentalColors.Primary)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Loading image...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Napier.d("DentalImagePreview: Image is loading...")
                    }
                }
            },
            error = {
                // CRITICAL: Show REAL error message for debugging
                Napier.e("DentalImagePreview: Failed to load image - ${it.result.throwable.message}")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Failed to load image",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Error: ${it.result.throwable.message ?: "Unknown error"}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            when (imageData) {
                                is ByteArray -> "Image size: ${imageData.size / 1024} KB"
                                is String -> "URL: ${imageData.take(50)}..."
                                else -> "Unknown image type"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            onSuccess = {
                when (imageData) {
                    is ByteArray -> Napier.i("DentalImagePreview: ByteArray image loaded successfully (${imageData.size} bytes)")
                    is String -> Napier.i("DentalImagePreview: URL image loaded successfully: $imageData")
                    else -> Napier.i("DentalImagePreview: Image loaded successfully")
                }
            }
        )

        // Zoom controls
        if (showZoomControls) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { scale = (scale * 1.2f).coerceAtMost(5f) },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Add, "Zoom in")
                }

                FloatingActionButton(
                    onClick = { scale = (scale / 1.2f).coerceAtLeast(0.5f) },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text("-", style = MaterialTheme.typography.headlineMedium)
                }

                if (scale != 1f) {
                    FloatingActionButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text("1x", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Close button
        if (onClose != null) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Close", tint = Color.White)
            }
        }

        // Scale indicator
        if (scale != 1f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "${(scale * 100).toInt()}%",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
