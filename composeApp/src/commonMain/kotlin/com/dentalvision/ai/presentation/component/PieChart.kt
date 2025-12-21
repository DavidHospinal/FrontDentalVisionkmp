package com.dentalvision.ai.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHoverGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun DonutChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    centerText: String = "",
    strokeWidth: Float = 50f
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val total = data.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data to display",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var hoveredSegment by remember { mutableStateOf<Int?>(null) }
    var hoverPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .pointerInput(Unit) {
                    detectHoverGestures(
                        onMove = { position ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f

                            val dx = position.x - centerX
                            val dy = position.y - centerY
                            val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                            val innerRadius = (size.width / 2f) - strokeWidth.dp.toPx()
                            val outerRadius = size.width / 2f

                            if (distance >= innerRadius && distance <= outerRadius) {
                                var angle = atan2(dy, dx) * 180f / PI.toFloat()
                                if (angle < 0) angle += 360f
                                angle = (angle + 90) % 360

                                var startAngle = 0f
                                var foundSegment: Int? = null

                                data.forEachIndexed { index, slice ->
                                    val sweepAngle = (slice.value / total) * 360f
                                    if (angle >= startAngle && angle < startAngle + sweepAngle) {
                                        foundSegment = index
                                    }
                                    startAngle += sweepAngle
                                }

                                hoveredSegment = foundSegment
                                hoverPosition = position
                            } else {
                                hoveredSegment = null
                            }
                        },
                        onExit = {
                            hoveredSegment = null
                        }
                    )
                }
        ) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            var startAngle = -90f

            data.forEachIndexed { index, slice ->
                val sweepAngle = (slice.value / total) * 360f
                val isHovered = hoveredSegment == index

                drawArc(
                    color = if (isHovered) slice.color.copy(alpha = 0.8f) else slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(
                        width = if (isHovered) strokeWidth.dp.toPx() * 1.1f else strokeWidth.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                startAngle += sweepAngle
            }
        }

        if (centerText.isNotEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = centerText,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 42.sp
                )
                Text(
                    text = "Overall Health",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        hoveredSegment?.let { index ->
            val slice = data[index]
            val percentage = (slice.value / total * 100).toInt()
            PieTooltip(
                text = "${slice.label}: ${slice.value.toInt()} (${percentage}%)",
                position = hoverPosition
            )
        }
    }
}

@Composable
private fun PieTooltip(
    text: String,
    position: Offset,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .offset(
                x = (position.x / 3).dp - 80.dp,
                y = (position.y / 3).dp - 100.dp
            )
            .widthIn(max = 180.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2C)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center
        )
    }
}
