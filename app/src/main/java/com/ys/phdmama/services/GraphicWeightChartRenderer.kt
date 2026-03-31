package com.ys.phdmama.services

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.ys.phdmama.model.LMS
import com.ys.phdmama.util.LmsUtils
import com.ys.phdmama.viewmodel.GrowthRecord

class GraphicWeightChartRenderer {
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

            val maxWeeks = 260f  // Weight data goes up to 60 weeks (approximately 60 months)
            val minWeight = 2f   // Minimum weight in kg
            val maxWeight = 30f  // Maximum weight in kg (to accommodate +3 SD line)

            val referenceData = if (sex.lowercase() == "girl") {
                LmsUtils.lmsWeightGirlsData
            } else {
                LmsUtils.lmsWeightBoysData
            }

            // Draw all chart elements
            drawBackground(sex, chartStartX, chartStartY, chartWidth, chartHeight)
            drawGrid(chartStartX, chartStartY, chartWidth, chartHeight, maxWeeks, minWeight, maxWeight)
            drawPercentileLines(referenceData, chartStartX, chartStartY, chartWidth, chartHeight, maxWeeks, minWeight, maxWeight, sex)
            drawZScoreLabels(chartStartX, chartStartY, chartWidth, chartHeight, referenceData, maxWeeks, minWeight, maxWeight)
            drawAxes(chartStartX, chartStartY, chartWidth, chartHeight, maxWeeks, minWeight, maxWeight)
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
        maxWeeks: Float,
        minWeight: Float,
        maxWeight: Float
    ) {
        val gridColorLight = Color(0x18B0B0B0)
        val gridColorMid   = Color(0x30A0A0A0)

        for (week in 0..maxWeeks.toInt()) {
            val isMajor = week % 52 == 0
            val isMid   = week % 26 == 0
            if (!isMajor && !isMid) continue

            val x = chartStartX + (week / maxWeeks) * chartWidth
            drawLine(
                color       = if (isMajor) gridColorMid else gridColorLight,
                start       = Offset(x, chartStartY + 8f),
                end         = Offset(x, chartStartY + chartHeight - 8f),
                strokeWidth = if (isMajor) 1.2f else 0.8f,
                pathEffect  = if (isMajor) null
                else PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }

        val stepSize = 2f
        val steps = ((maxWeight - minWeight) / stepSize).toInt()
        for (step in 0..steps) {
            val value   = minWeight + step * stepSize
            val y       = chartStartY + chartHeight -
                    ((value - minWeight) / (maxWeight - minWeight)) * chartHeight
            val isMajor = step % 2 == 0  // principales cada 4 kg

            drawLine(
                color       = if (isMajor) gridColorMid else gridColorLight,
                start       = Offset(chartStartX + 8f, y),
                end         = Offset(chartStartX + chartWidth - 8f, y),
                strokeWidth = if (isMajor) 1.2f else 0.8f,
                pathEffect  = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )
        }
    }

    private fun DrawScope.drawPercentileLines(
        data: List<LMS>,
        chartStartX: Float,
        chartStartY: Float,
        chartWidth: Float,
        chartHeight: Float,
        maxWeeks: Float,
        minWeight: Float,
        maxWeight: Float,
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
                maxWeeks = maxWeeks,
                minWeight = minWeight,
                maxWeight = maxWeight,
                color = line.color,
                strokeWidth = line.strokeWidth,
                zScore = line.zScore
            )
        }
    }

    private fun DrawScope.drawPercentileLine(
        data: List<LMS>,
        chartStartX: Float,
        chartStartY: Float,
        chartWidth: Float,
        chartHeight: Float,
        maxWeeks: Float,
        minWeight: Float,
        maxWeight: Float,
        color: Color,
        strokeWidth: Float,
        zScore: Double
    ) {
        val path = Path()
        var isFirstPoint = true

        data.forEach { lms ->
            val x = chartStartX + (lms.week / maxWeeks) * chartWidth
            val value = if (zScore == 0.0) {
                lms.M
            } else {
                calculatePercentileFromLMS(lms.L, lms.M, lms.S, zScore)
            }.toFloat()

            val y = chartStartY + chartHeight - ((value - minWeight) / (maxWeight - minWeight)) * chartHeight

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
        referenceData: List<LMS>,
        maxWeeks: Float,
        minWeight: Float,
        maxWeight: Float
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
            val y = chartStartY + chartHeight - ((value - minWeight) / (maxWeight - minWeight)) * chartHeight

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
        maxWeeks: Float,
        minWeight: Float,
        maxWeight: Float
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

        // X-axis labels (show months, every 6 months)
        // 1 month ≈ 4.345 weeks
        for (month in 0..60 step 6) {
            val week = (month * 4.345).toFloat()
            val x = chartStartX + (week / maxWeeks) * chartWidth
            drawContext.canvas.nativeCanvas.drawText(
                "$month",
                x - 10f,
                chartStartY + chartHeight + 30f,
                textPaint
            )
        }

        // Y-axis labels (every 2 kg)
        val stepSize = 2f
        val steps = ((maxWeight - minWeight) / stepSize).toInt()
        for (step in 0..steps) {
            val value = minWeight + step * stepSize
            val y = chartStartY + chartHeight - ((value - minWeight) / (maxWeight - minWeight)) * chartHeight
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
            "Peso (kg)",
            -40f,
            chartStartY + chartHeight / 2,
            titlePaint
        )
        drawContext.canvas.nativeCanvas.restore()
    }

    fun monthsToWeek(months: Int): Int {
        return kotlin.math.round(months * 4.345).toInt()
    }

    private fun calculatePercentileFromLMS(L: Double, M: Double, S: Double, zScore: Double): Double {
        return M * Math.pow(1.0 + L * S * zScore, 1.0 / L)
    }

    private data class PercentileLine(
        val zScore: Double,
        val color: Color,
        val strokeWidth: Float
    )
}
