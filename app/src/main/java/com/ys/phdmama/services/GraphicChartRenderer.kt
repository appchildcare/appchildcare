package com.ys.phdmama.services

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.ys.phdmama.model.LMSHeightLength
import com.ys.phdmama.ui.screens.born.charts.calculatePercentileFromLMS
import com.ys.phdmama.util.LmsUtils
import com.ys.phdmama.viewmodel.GrowthRecord

class GraphicChartRenderer {
    fun drawChart(
        drawScope: DrawScope,
        records: List<GrowthRecord>,
        sex: String,
        size: Size
    ) {
        with(drawScope) {
            val chartWidth = size.width - 120f
            val chartHeight = size.height - 120f
            val chartStartX = 80f
            val chartStartY = 40f

            val maxMonths = 13f
            val minHeight = 45f
            val maxHeight = 85f

            //TODO: to make it generic pass the values referenceData as parameter
            val referenceData = if (sex.lowercase() == "girl") {
                LmsUtils.lmdGirlsHeightLengthData
            } else {
                LmsUtils.lmdBoysHeightLengthData
            }

            // Draw all chart elements
            drawBackground(sex, chartStartX, chartStartY, chartWidth, chartHeight)
            drawGrid(chartStartX, chartStartY, chartWidth, chartHeight, maxMonths, minHeight, maxHeight)
            drawPercentileLines(referenceData, chartStartX, chartStartY, chartWidth, chartHeight, maxMonths, minHeight, maxHeight, sex)
            drawZScoreLabels(chartStartX, chartStartY, chartWidth, chartHeight, referenceData, maxMonths, minHeight, maxHeight)
            drawAxes(chartStartX, chartStartY, chartWidth, chartHeight, maxMonths, minHeight, maxHeight)
        }
    }

    private fun DrawScope.drawBackground(
        sex: String,
        chartStartX: Float,
        chartStartY: Float,
        chartWidth: Float,
        chartHeight: Float
    ) {
        val backgroundColor = if (sex.lowercase() == "girl") {
            Color(0xFFFCF0FC)
        } else {
            Color(0xFFF0F8FF)
        }

        drawRect(
            color = backgroundColor,
            topLeft = Offset(chartStartX, chartStartY),
            size = Size(chartWidth, chartHeight)
        )
    }

    private fun DrawScope.drawGrid(
        chartStartX: Float,
        chartStartY: Float,
        chartWidth: Float,
        chartHeight: Float,
        maxMonths: Float,
        minHeight: Float,
        maxHeight: Float
    ) {
        val gridColor = Color(0xFFE0E0E0)

        // Vertical grid lines (months)
        for (month in 0..maxMonths.toInt()) {
            val x = chartStartX + (month / maxMonths) * chartWidth
            drawLine(
                color = gridColor,
                start = Offset(x, chartStartY),
                end = Offset(x, chartStartY + chartHeight),
                strokeWidth = 1f
            )
        }

        // Horizontal grid lines (height)
        val stepSize = 2f
        val steps = ((maxHeight - minHeight) / stepSize).toInt()
        for (step in 0..steps) {
            val value = minHeight + step * stepSize
            val y = chartStartY + chartHeight - ((value - minHeight) / (maxHeight - minHeight)) * chartHeight
            drawLine(
                color = gridColor,
                start = Offset(chartStartX, y),
                end = Offset(chartStartX + chartWidth, y),
                strokeWidth = 1f
            )
        }
    }

    private fun DrawScope.drawPercentileLines(
        data: List<LMSHeightLength>,
        chartStartX: Float,
        chartStartY: Float,
        chartWidth: Float,
        chartHeight: Float,
        maxMonths: Float,
        minHeight: Float,
        maxHeight: Float,
        sex: String
    ) {
        val percentileLines = listOf(
            PercentileLine(3.0, Color.Black, 3f),
            PercentileLine(2.0, Color.Red, 2f),
            PercentileLine(0.0, Color(0xFF4CAF50), 3f),
            PercentileLine(-2.0, Color.Red, 2f),
            PercentileLine(-3.0, Color.Black, 3f)
        )

        percentileLines.forEach { line ->
            drawPercentileLine(
                data = data,
                chartStartX = chartStartX,
                chartStartY = chartStartY,
                chartWidth = chartWidth,
                chartHeight = chartHeight,
                maxMonths = maxMonths,
                minHeight = minHeight,
                maxHeight = maxHeight,
                color = line.color,
                strokeWidth = line.strokeWidth,
                zScore = line.zScore
            )
        }
    }

    private fun DrawScope.drawPercentileLine(
        data: List<LMSHeightLength>,
        chartStartX: Float,
        chartStartY: Float,
        chartWidth: Float,
        chartHeight: Float,
        maxMonths: Float,
        minHeight: Float,
        maxHeight: Float,
        color: Color,
        strokeWidth: Float,
        zScore: Double
    ) {
        val path = Path()
        var isFirstPoint = true

        data.forEach { lms ->
            val x = chartStartX + (lms.month / maxMonths) * chartWidth
            val value = if (zScore == 0.0) {
                lms.M
            } else {
                calculatePercentileFromLMS(lms.L, lms.M, lms.S, zScore)
            }.toFloat()

            val y = chartStartY + chartHeight - ((value - minHeight) / (maxHeight - minHeight)) * chartHeight

            if (isFirstPoint) {
                path.moveTo(x, y)
                isFirstPoint = false
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }

    private fun DrawScope.drawZScoreLabels(
        chartStartX: Float,
        chartStartY: Float,
        chartWidth: Float,
        chartHeight: Float,
        referenceData: List<LMSHeightLength>,
        maxMonths: Float,
        minHeight: Float,
        maxHeight: Float
    ) {
        val textPaint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = 32f
            color = android.graphics.Color.BLACK
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val lastLMS = referenceData.last()
        val labelX = chartStartX + chartWidth + 10f

        val zScores = listOf(3.0, 2.0, 0.0, -2.0, -3.0)
        val colors = listOf(
            android.graphics.Color.BLACK,
            android.graphics.Color.RED,
            android.graphics.Color.parseColor("#4CAF50"),
            android.graphics.Color.RED,
            android.graphics.Color.BLACK
        )

        zScores.forEachIndexed { index, zScore ->
            val value = calculatePercentileFromLMS(lastLMS.L, lastLMS.M, lastLMS.S, zScore).toFloat()
            val y = chartStartY + chartHeight - ((value - minHeight) / (maxHeight - minHeight)) * chartHeight

            textPaint.color = colors[index]
            drawContext.canvas.nativeCanvas.drawText(
                if (zScore > 0) "+${zScore.toInt()}" else zScore.toInt().toString(),
                labelX,
                y + 8f,
                textPaint
            )
        }
    }

    private fun DrawScope.drawAxes(
        chartStartX: Float,
        chartStartY: Float,
        chartWidth: Float,
        chartHeight: Float,
        maxMonths: Float,
        minHeight: Float,
        maxHeight: Float
    ) {
        val axisColor = Color.Black
        val textPaint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = 24f
            color = android.graphics.Color.BLACK
        }

        // X-axis
        drawLine(
            color = axisColor,
            start = Offset(chartStartX, chartStartY + chartHeight),
            end = Offset(chartStartX + chartWidth, chartStartY + chartHeight),
            strokeWidth = 2f
        )

        // Y-axis
        drawLine(
            color = axisColor,
            start = Offset(chartStartX, chartStartY),
            end = Offset(chartStartX, chartStartY + chartHeight),
            strokeWidth = 2f
        )

        // X-axis labels
        for (month in 0..maxMonths.toInt()) {
            val x = chartStartX + (month / maxMonths) * chartWidth
            val label = if (month == 0) "0" else "$month"
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x - 10f,
                chartStartY + chartHeight + 30f,
                textPaint
            )
        }

        // Y-axis labels
        val stepSize = 5f
        val steps = ((maxHeight - minHeight) / stepSize).toInt()
        for (step in 0..steps) {
            val value = minHeight + step * stepSize
            val y = chartStartY + chartHeight - ((value - minHeight) / (maxHeight - minHeight)) * chartHeight
            drawContext.canvas.nativeCanvas.drawText(
                "${value.toInt()}",
                chartStartX - 50f,
                y + 8f,
                textPaint
            )
        }

        // Axis titles
        val titlePaint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = 28f
            color = android.graphics.Color.BLACK
            isFakeBoldText = true
        }

        drawContext.canvas.nativeCanvas.drawText(
            "Edad (meses)",
            chartStartX + chartWidth / 2 - 60f,
            chartStartY + chartHeight + 70f,
            titlePaint
        )

        drawContext.canvas.nativeCanvas.save()
        drawContext.canvas.nativeCanvas.rotate(-90f, 25f, chartStartY + chartHeight / 2)
        drawContext.canvas.nativeCanvas.drawText(
            "Longitud/Talla (cm)",
            -80f,
            chartStartY + chartHeight / 2,
            titlePaint
        )
        drawContext.canvas.nativeCanvas.restore()
    }

    private data class PercentileLine(
        val zScore: Double,
        val color: Color,
        val strokeWidth: Float
    )
}