package com.ys.cunaco.services

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import com.ys.cunaco.model.LMS
import com.ys.cunaco.util.LmsUtils
import com.ys.cunaco.viewmodel.GrowthRecord

class GraphicWeightChartRenderer {
    // ── Eje Y fijo (solo etiquetas Y + título "Peso (kg)") ───────────────────
    fun drawYAxisOnly(
        drawScope: DrawScope,
        records: List<GrowthRecord>,
        sex: String,
        size: Size
    ) {
        with(drawScope) {
            val paddingTop    = 40f
            val paddingBottom = 80f
            val minWeight     = 2f
            val maxWeight     = 30f
            val chartHeight   = size.height - paddingTop - paddingBottom

            val textPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize    = 24f
                color       = android.graphics.Color.BLACK
                textAlign   = android.graphics.Paint.Align.RIGHT
            }

            // Línea del eje Y
            drawLine(
                color       = Color.Black,
                start       = Offset(size.width - 2f, paddingTop),
                end         = Offset(size.width - 2f, paddingTop + chartHeight),
                strokeWidth = 2f
            )

            // Etiquetas cada 2 kg
            val stepSize = 2f
            val steps    = ((maxWeight - minWeight) / stepSize).toInt()
            for (step in 0..steps) {
                val value = minWeight + step * stepSize
                val y     = paddingTop + chartHeight -
                        ((value - minWeight) / (maxWeight - minWeight)) * chartHeight
                drawContext.canvas.nativeCanvas.drawText(
                    "${value.toInt()}",
                    size.width - 6f,
                    y + 8f,
                    textPaint
                )
            }

            // Título "Peso (kg)" rotado
            val titlePaint = android.graphics.Paint().apply {
                isAntiAlias  = true
                textSize     = 28f
                color        = android.graphics.Color.BLACK
                isFakeBoldText = true
                textAlign    = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.rotate(
                -90f,
                size.width / 2f,
                size.height / 2f
            )
            drawContext.canvas.nativeCanvas.drawText(
                "Peso (kg)",
                size.height / 2f,
                size.width / 2f - 4f,
                titlePaint
            )
            drawContext.canvas.nativeCanvas.restore()
        }
    }

    // ── Área scrollable: fondo + cuadrícula + curvas OMS + puntos + eje X ───
    fun drawChartWithoutYAxis(
        drawScope: DrawScope,
        records: List<GrowthRecord>,
        sex: String,
        size: Size,
        maxMonths: Int = 60
    ) {
        with(drawScope) {
            val paddingTop    = 40f
            val paddingBottom = 80f
            val paddingLeft   = 8f
            val paddingRight  = 50f

            val chartWidth  = size.width  - paddingLeft - paddingRight
            val chartHeight = size.height - paddingTop  - paddingBottom

            val minWeight = 2f
            val maxWeight = 30f

            val referenceData = if (sex.lowercase() == "girl")
                LmsUtils.lmsWeightGirlsData else LmsUtils.lmsWeightBoysData

            // ✅ Use real weeks so LMS data and baby points share the same scale
            val maxMonths = 60f
            //val maxRecordMonth = records.maxOfOrNull { it.ageInMonths }?.toFloat() ?: 60f
            //val filteredData = referenceData.filter { it.week <= maxRecordMonth * 4.345f }

            drawBackground(sex, paddingLeft, paddingTop, chartWidth, chartHeight)
            drawGrid(paddingLeft, paddingTop, chartWidth, chartHeight, maxMonths, minWeight, maxWeight)
            drawPercentileLines(referenceData, paddingLeft, paddingTop, chartWidth, chartHeight, maxMonths, minWeight, maxWeight, sex)
            drawZScoreLabels(paddingLeft, paddingTop, chartWidth, chartHeight, referenceData, maxMonths, minWeight, maxWeight)

            // Eje X
            drawLine(
                color       = Color.Black,
                start       = Offset(paddingLeft, paddingTop + chartHeight),
                end         = Offset(paddingLeft + chartWidth, paddingTop + chartHeight),
                strokeWidth = 2f
            )

            val axisTextPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize    = 24f
                color       = android.graphics.Color.BLACK
                textAlign   = android.graphics.Paint.Align.CENTER
            }

            // ✅ Convert months → weeks for correct X position on axis labels
            for (month in 0..60 step 6) {
                val x = paddingLeft + (month / maxMonths) * chartWidth
                drawLine(
                    color       = Color.Black,
                    start       = Offset(x, paddingTop + chartHeight),
                    end         = Offset(x, paddingTop + chartHeight + 8f),
                    strokeWidth = 1.5f
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "$month", x, paddingTop + chartHeight + 30f, axisTextPaint
                )
            }

            val titlePaint = android.graphics.Paint().apply {
                isAntiAlias    = true
                textSize       = 28f
                color          = android.graphics.Color.BLACK
                isFakeBoldText = true
                textAlign      = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(
                "Edad (meses)",
                paddingLeft + chartWidth / 2f,
                paddingTop + chartHeight + 68f,
                titlePaint
            )

            if (records.isNotEmpty()) {
                // Add this before calling drawBabyDataPoints
                clipRect(
                    left   = paddingLeft,
                    top    = paddingTop,
                    right  = paddingLeft + chartWidth,
                    bottom = paddingTop + chartHeight
                ) {
                    drawBabyDataPoints(
                        records     = records,
                        chartStartX = paddingLeft,
                        chartStartY = paddingTop,
                        chartWidth  = chartWidth,
                        chartHeight = chartHeight,
                        maxWeeks    = maxMonths,
                        minWeight   = minWeight,
                        maxWeight   = maxWeight
                    )
                }
            }
        }
    }

    private fun DrawScope.drawBabyDataPoints(
        records: List<GrowthRecord>,
        chartStartX: Float,
        chartStartY: Float,
        chartWidth: Float,
        chartHeight: Float,
        maxWeeks: Float,
        minWeight: Float,
        maxWeight: Float
    ) {
        val sortedRecords = records.sortedBy { it.ageInMonths }

        if (sortedRecords.size > 1) {
            val linePath = Path()
            sortedRecords.forEachIndexed { index, record ->
                val weight = record.weight ?: return@forEachIndexed
                val x = chartStartX + (record.ageInMonths.toFloat() / maxWeeks) * chartWidth  // ✅
                val y = chartStartY + chartHeight -
                        ((weight - minWeight) / (maxWeight - minWeight)).toFloat() * chartHeight

                if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
            }
            drawPath(path = linePath, color = Color(0xFF1565C0), style = Stroke(width = 2f))
        }


        sortedRecords.forEach { record ->
            val weight = record.weight ?: return@forEach
            val x = chartStartX + (record.ageInMonths.toFloat() / maxWeeks) * chartWidth  // ✅
            val y = chartStartY + chartHeight -
                    ((weight - minWeight) / (maxWeight - minWeight)).toFloat() * chartHeight

            drawCircle(color = Color(0xFF1565C0).copy(alpha = 0.25f), radius = 10f, center = Offset(x, y))
            drawCircle(color = Color(0xFF1976D2), radius = 6f, center = Offset(x, y))
            drawCircle(color = Color.White, radius = 2.5f, center = Offset(x, y))
        }
    }

    // ── Métodos privados existentes (sin cambios) ────────────────────────────

    private fun DrawScope.drawBackground(
        sex: String,
        chartStartX: Float,
        chartStartY: Float,
        chartWidth: Float,
        chartHeight: Float
    ) {
        val backgroundColor = if (sex.lowercase() == "girl") Color(0xFFFCF0FC)
        else Color(0xFFF0F8FF)
        drawRect(
            color    = backgroundColor,
            topLeft  = Offset(chartStartX, chartStartY),
            size     = Size(chartWidth, chartHeight)
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
        val steps    = ((maxWeight - minWeight) / stepSize).toInt()
        for (step in 0..steps) {
            val value   = minWeight + step * stepSize
            val y       = chartStartY + chartHeight -
                    ((value - minWeight) / (maxWeight - minWeight)) * chartHeight
            val isMajor = step % 2 == 0

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
            PercentileLine( 3.0, Color.Black,            3f),
            PercentileLine( 2.0, Color(0xFFFF6F00),      2f),  // naranja oscuro
            PercentileLine( 0.0, Color(0xFF4CAF50),      3f),  // verde (sin cambio)
            PercentileLine(-2.0, Color(0xFFFF6F00),      2f),  // naranja oscuro
            PercentileLine(-3.0, Color.Black,            3f)
        )
        percentileLines.forEach { line ->
            drawPercentileLine(
                data        = data,
                chartStartX = chartStartX,
                chartStartY = chartStartY,
                chartWidth  = chartWidth,
                chartHeight = chartHeight,
                maxWeeks    = maxWeeks,
                minWeight   = minWeight,
                maxWeight   = maxWeight,
                color       = line.color,
                strokeWidth = line.strokeWidth,
                zScore      = line.zScore
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
        val path         = Path()
        var isFirstPoint = true

        data.forEach { lms ->
            val x = chartStartX + (lms.week / maxWeeks) * chartWidth
            val value = if (zScore == 0.0) lms.M
            else calculatePercentileFromLMS(lms.L, lms.M, lms.S, zScore)
            val y = chartStartY + chartHeight -
                    ((value.toFloat() - minWeight) / (maxWeight - minWeight)) * chartHeight

            if (isFirstPoint) { path.moveTo(x, y); isFirstPoint = false }
            else               path.lineTo(x, y)
        }

        drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
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
            isAntiAlias    = true
            textSize       = 32f
            color          = android.graphics.Color.BLACK
            typeface       = android.graphics.Typeface.DEFAULT_BOLD
        }

        val lastLMS = referenceData.last()
        val labelX  = chartStartX + chartWidth + 10f

        val zScores = listOf(3.0, 2.0, 0.0, -2.0, -3.0)
        val colors  = listOf(
            android.graphics.Color.BLACK,
            android.graphics.Color.parseColor("#FF6F00"),
            android.graphics.Color.parseColor("#4CAF50"),
            android.graphics.Color.parseColor("#FF6F00"),
            android.graphics.Color.BLACK
        )

        zScores.forEachIndexed { index, zScore ->
            val value = calculatePercentileFromLMS(lastLMS.L, lastLMS.M, lastLMS.S, zScore).toFloat()
            val y     = chartStartY + chartHeight -
                    ((value - minWeight) / (maxWeight - minWeight)) * chartHeight

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
            textSize    = 24f
            color       = android.graphics.Color.BLACK
        }

        drawLine(color = axisColor,
            start = Offset(chartStartX, chartStartY + chartHeight),
            end   = Offset(chartStartX + chartWidth, chartStartY + chartHeight),
            strokeWidth = 2f)

        drawLine(color = axisColor,
            start = Offset(chartStartX, chartStartY),
            end   = Offset(chartStartX, chartStartY + chartHeight),
            strokeWidth = 2f)

        for (month in 0..60 step 6) {
            val week = (month * 4.345).toFloat()
            val x    = chartStartX + (week / maxWeeks) * chartWidth
            drawContext.canvas.nativeCanvas.drawText("$month", x - 10f,
                chartStartY + chartHeight + 30f, textPaint)
        }

        val stepSize = 2f
        val steps    = ((maxWeight - minWeight) / stepSize).toInt()
        for (step in 0..steps) {
            val value = minWeight + step * stepSize
            val y     = chartStartY + chartHeight -
                    ((value - minWeight) / (maxWeight - minWeight)) * chartHeight
            drawContext.canvas.nativeCanvas.drawText("${value.toInt()}",
                chartStartX - 50f, y + 8f, textPaint)
        }

        val titlePaint = Paint().asFrameworkPaint().apply {
            isAntiAlias    = true
            textSize       = 28f
            color          = android.graphics.Color.BLACK
            isFakeBoldText = true
        }
        drawContext.canvas.nativeCanvas.drawText("Edad (meses)",
            chartStartX + chartWidth / 2 - 60f,
            chartStartY + chartHeight + 70f, titlePaint)

        drawContext.canvas.nativeCanvas.save()
        drawContext.canvas.nativeCanvas.rotate(-90f, 25f, chartStartY + chartHeight / 2)
        drawContext.canvas.nativeCanvas.drawText("Peso (kg)",
            -40f, chartStartY + chartHeight / 2, titlePaint)
        drawContext.canvas.nativeCanvas.restore()
    }

    fun monthsToWeek(months: Int): Int = kotlin.math.round(months * 4.345).toInt()

    private fun calculatePercentileFromLMS(L: Double, M: Double, S: Double, zScore: Double): Double =
        M * Math.pow(1.0 + L * S * zScore, 1.0 / L)

    private data class PercentileLine(
        val zScore: Double,
        val color: Color,
        val strokeWidth: Float
    )
}
