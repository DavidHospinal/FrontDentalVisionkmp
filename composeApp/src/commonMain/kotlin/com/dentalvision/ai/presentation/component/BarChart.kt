package com.dentalvision.ai.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dentalvision.ai.presentation.theme.DentalColors

data class BarChartData(
    val label: String,
    val value: Float,
    val color: Color
)

data class DoubleBarChartData(
    val label: String,
    val value1: Float,
    val value2: Float,
    val color1: Color,
    val color2: Color,
    val label1: String = "Primary",
    val label2: String = "Secondary"
)

@Composable
fun BarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    maxValue: Float? = null,
    barColor: Color = DentalColors.Primary,
    showValues: Boolean = true
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

    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelSmall
    val valueTextStyle = MaterialTheme.typography.labelMedium

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val computedMaxValue = maxValue ?: (data.maxOfOrNull { it.value } ?: 1f)

    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    var hoverPosition by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(Unit) {
                    detectHoverGestures(
                        onMove = { position ->
                            val canvasWidth = size.width
                            val barWidth = (canvasWidth / data.size) * 0.6f
                            val spacing = (canvasWidth / data.size) * 0.4f

                            val index = ((position.x - (spacing / 2)) / (barWidth + spacing)).toInt()
                            hoveredIndex = if (index in data.indices) index else null
                            hoverPosition = position
                        },
                        onExit = {
                            hoveredIndex = null
                        }
                    )
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val bottomPadding = 40.dp.toPx()
            val topPadding = 30.dp.toPx()
            val chartHeight = canvasHeight - bottomPadding - topPadding

            val barWidth = (canvasWidth / data.size) * 0.6f
            val spacing = (canvasWidth / data.size) * 0.4f

            data.forEachIndexed { index, chartData ->
                val barHeight = (chartData.value / computedMaxValue) * chartHeight
                val x = (index * (barWidth + spacing)) + (spacing / 2)
                val y = topPadding + (chartHeight - barHeight)

                val isHovered = hoveredIndex == index

                drawRoundRect(
                    color = if (isHovered) chartData.color.copy(alpha = 0.8f) else chartData.color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                if (showValues) {
                    val valueText = chartData.value.toInt().toString()
                    val textLayoutResult = textMeasurer.measure(
                        text = valueText,
                        style = valueTextStyle
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = valueText,
                        topLeft = Offset(
                            x = x + (barWidth / 2) - (textLayoutResult.size.width / 2),
                            y = y - textLayoutResult.size.height - 4.dp.toPx()
                        ),
                        style = valueTextStyle.copy(color = onSurfaceColor)
                    )
                }

                val labelLayoutResult = textMeasurer.measure(
                    text = chartData.label,
                    style = textStyle
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = chartData.label,
                    topLeft = Offset(
                        x = x + (barWidth / 2) - (labelLayoutResult.size.width / 2),
                        y = canvasHeight - bottomPadding + 8.dp.toPx()
                    ),
                    style = textStyle.copy(color = onSurfaceVariantColor)
                )
            }

            drawLine(
                color = onSurfaceVariantColor.copy(alpha = 0.3f),
                start = Offset(0f, topPadding + chartHeight),
                end = Offset(canvasWidth, topPadding + chartHeight),
                strokeWidth = 1.dp.toPx()
            )
        }

        hoveredIndex?.let { index ->
            val chartData = data[index]
            Tooltip(
                text = "${chartData.label}: ${chartData.value.toInt()}",
                position = hoverPosition
            )
        }
    }
}

@Composable
fun DoubleBarChart(
    data: List<DoubleBarChartData>,
    modifier: Modifier = Modifier,
    maxValue: Float? = null,
    showValues: Boolean = true
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

    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelSmall
    val valueTextStyle = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp)

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val computedMaxValue = maxValue ?: (data.maxOfOrNull { maxOf(it.value1, it.value2) } ?: 1f)

    var hoveredBar by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var hoverPosition by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(Unit) {
                    detectHoverGestures(
                        onMove = { position ->
                            val canvasWidth = size.width
                            val groupWidth = canvasWidth / data.size
                            val barWidth = (groupWidth * 0.6f) / 2
                            val spacing = groupWidth * 0.4f

                            val groupIndex = (position.x / groupWidth).toInt()
                            if (groupIndex in data.indices) {
                                val groupStartX = groupIndex * groupWidth + (spacing / 2)
                                val bar1X = groupStartX
                                val bar2X = groupStartX + barWidth + 4.dp.toPx()

                                val barHovered = when {
                                    position.x >= bar1X && position.x < bar1X + barWidth -> 1
                                    position.x >= bar2X && position.x < bar2X + barWidth -> 2
                                    else -> null
                                }

                                hoveredBar = if (barHovered != null) Pair(groupIndex, barHovered) else null
                                hoverPosition = position
                            } else {
                                hoveredBar = null
                            }
                        },
                        onExit = {
                            hoveredBar = null
                        }
                    )
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val bottomPadding = 40.dp.toPx()
            val topPadding = 30.dp.toPx()
            val chartHeight = canvasHeight - bottomPadding - topPadding

            val groupWidth = canvasWidth / data.size
            val barWidth = (groupWidth * 0.6f) / 2
            val spacing = groupWidth * 0.4f

            data.forEachIndexed { index, chartData ->
                val groupX = index * groupWidth + (spacing / 2)

                val barHeight1 = (chartData.value1 / computedMaxValue) * chartHeight
                val y1 = topPadding + (chartHeight - barHeight1)

                val isBar1Hovered = hoveredBar == Pair(index, 1)

                drawRoundRect(
                    color = if (isBar1Hovered) chartData.color1.copy(alpha = 0.8f) else chartData.color1,
                    topLeft = Offset(groupX, y1),
                    size = Size(barWidth, barHeight1),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                if (showValues && chartData.value1 > 0) {
                    val valueText = chartData.value1.toInt().toString()
                    val textLayoutResult = textMeasurer.measure(
                        text = valueText,
                        style = valueTextStyle
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = valueText,
                        topLeft = Offset(
                            x = groupX + (barWidth / 2) - (textLayoutResult.size.width / 2),
                            y = y1 - textLayoutResult.size.height - 2.dp.toPx()
                        ),
                        style = valueTextStyle.copy(color = onSurfaceColor)
                    )
                }

                val barHeight2 = (chartData.value2 / computedMaxValue) * chartHeight
                val y2 = topPadding + (chartHeight - barHeight2)

                val isBar2Hovered = hoveredBar == Pair(index, 2)

                drawRoundRect(
                    color = if (isBar2Hovered) chartData.color2.copy(alpha = 0.8f) else chartData.color2,
                    topLeft = Offset(groupX + barWidth + 4.dp.toPx(), y2),
                    size = Size(barWidth, barHeight2),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                if (showValues && chartData.value2 > 0) {
                    val valueText = chartData.value2.toInt().toString()
                    val textLayoutResult = textMeasurer.measure(
                        text = valueText,
                        style = valueTextStyle
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = valueText,
                        topLeft = Offset(
                            x = groupX + barWidth + 4.dp.toPx() + (barWidth / 2) - (textLayoutResult.size.width / 2),
                            y = y2 - textLayoutResult.size.height - 2.dp.toPx()
                        ),
                        style = valueTextStyle.copy(color = onSurfaceColor)
                    )
                }

                val labelLayoutResult = textMeasurer.measure(
                    text = chartData.label,
                    style = textStyle
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = chartData.label,
                    topLeft = Offset(
                        x = groupX + (barWidth + 4.dp.toPx()) - (labelLayoutResult.size.width / 2),
                        y = canvasHeight - bottomPadding + 8.dp.toPx()
                    ),
                    style = textStyle.copy(color = onSurfaceVariantColor)
                )
            }

            drawLine(
                color = onSurfaceVariantColor.copy(alpha = 0.3f),
                start = Offset(0f, topPadding + chartHeight),
                end = Offset(canvasWidth, topPadding + chartHeight),
                strokeWidth = 1.dp.toPx()
            )
        }

        hoveredBar?.let { (groupIndex, barIndex) ->
            val chartData = data[groupIndex]
            val value = if (barIndex == 1) chartData.value1 else chartData.value2
            val label = if (barIndex == 1) chartData.label1 else chartData.label2
            Tooltip(
                text = "${chartData.label} - $label: ${value.toInt()}",
                position = hoverPosition
            )
        }
    }
}

@Composable
private fun Tooltip(
    text: String,
    position: Offset,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .offset(
                x = (position.x / 2).dp - 60.dp,
                y = (position.y / 2).dp - 40.dp
            )
            .widthIn(max = 150.dp),
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
            modifier = Modifier.padding(8.dp)
        )
    }
}
