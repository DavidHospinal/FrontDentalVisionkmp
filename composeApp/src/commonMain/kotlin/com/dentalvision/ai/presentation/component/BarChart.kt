package com.dentalvision.ai.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.dentalvision.ai.presentation.theme.DentalColors

data class BarChartData(
    val label: String,
    val value: Float,
    val color: Color
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

    // Capture colors outside Canvas (before DrawScope)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val computedMaxValue = maxValue ?: (data.maxOfOrNull { it.value } ?: 1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
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

            // Draw bar
            drawRoundRect(
                color = chartData.color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Draw value on top of bar
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

            // Draw label below bar
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

        // Draw baseline
        drawLine(
            color = onSurfaceVariantColor.copy(alpha = 0.3f),
            start = Offset(0f, topPadding + chartHeight),
            end = Offset(canvasWidth, topPadding + chartHeight),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawText(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    text: String,
    topLeft: Offset,
    style: TextStyle
) {
    val textLayoutResult = textMeasurer.measure(text, style)
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = topLeft
    )
}
