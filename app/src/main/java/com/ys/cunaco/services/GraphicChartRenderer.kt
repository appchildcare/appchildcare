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
import com.ys.cunaco.model.LMSHeightLength
import com.ys.cunaco.ui.screens.born.charts.calculatePercentileFromLMS
import com.ys.cunaco.util.LmsUtils
import com.ys.cunaco.viewmodel.GrowthRecord

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
            val minHeight = 30f // Ajustado de 45f a 30f para mostrar el punto de 34cm
            val maxHeight = 85f

            // Datos de referencia (OMS)
            val referenceData = if (sex.lowercase() == "girl") {
                LmsUtils.lmdGirlsHeightLengthData
            } else {
                LmsUtils.lmsBoysHeightLengthData
            }

            // Dibujar elementos estáticos
            drawBackground(sex, chartStartX, chartStartY, chartWidth, chartHeight)
            drawGrid(chartStartX, chartStartY, chartWidth, chartHeight, maxMonths, minHeight, maxHeight)
            drawPercentileLines(referenceData, chartStartX, chartStartY, chartWidth, chartHeight, maxMonths, minHeight, maxHeight, sex)
            drawZScoreLabels(chartStartX, chartStartY, chartWidth, chartHeight, referenceData, maxMonths, minHeight, maxHeight)
            drawAxes(chartStartX, chartStartY, chartWidth, chartHeight, maxMonths, minHeight, maxHeight)
            
            // Dibujar los puntos del paciente (Firebase)
            if (records.isNotEmpty()) {
                clipRect(
                    left = chartStartX,
                    top = chartStartY,
                    right = chartStartX + chartWidth,
                    bottom = chartStartY + chartHeight
                ) {
                    plotPatientData(
                        records = records,
                        chartStartX = chartStartX,
                        chartStartY = chartStartY,
                        chartWidth = chartWidth,
                        chartHeight = chartHeight,
                        maxMonths = maxMonths,
                        minHeight = minHeight,
                        maxHeight = maxHeight
                    )
                }
            }
        }
    }

    private fun DrawScope.drawBackground(
        sex: String,
        chartStartX: Float,
        chartStartY: Float,
        chartWidth: Float,
        chartHeight: Float
    ) {
        val backgroundColor = if (sex.lowercase() == "girl") Color(0xFFFCF0FC) else Color(0xFFF0F8FF)
        drawRect(color = backgroundColor, topLeft = Offset(chartStartX, chartStartY), size = Size(chartWidth, chartHeight))
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
        val gridColorMid = Color(0x30A0A0A0)
        
        // Grid vertical (meses)
        for (month in 0..maxMonths.toInt()) {
            val x = chartStartX + (month / maxMonths) * chartWidth
            drawLine(color = gridColorMid, start = Offset(x, chartStartY), end = Offset(x, chartStartY + chartHeight), strokeWidth = 1f)
        }

        // Grid horizontal (talla)
        val stepSize = 5f
        val steps = ((maxHeight - minHeight) / stepSize).toInt()
        for (step in 0..steps) {
            val value = minHeight + step * stepSize
            val y = chartStartY + chartHeight - ((value - minHeight) / (maxHeight - minHeight)) * chartHeight
            drawLine(color = gridColorMid, start = Offset(chartStartX, y), end = Offset(chartStartX + chartWidth, y), strokeWidth = 1f)
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
        val zScores = listOf(3.0, 2.0, 0.0, -2.0, -3.0)
        val colors = listOf(Color.Black, Color.Red, Color(0xFF4CAF50), Color.Red, Color.Black)

        zScores.forEachIndexed { i, z ->
            val path = Path()
            data.forEachIndexed { index, lms ->
                val x = chartStartX + (lms.month / maxMonths) * chartWidth
                val value = calculatePercentileFromLMS(lms.L, lms.M, lms.S, z).toFloat()
                val y = chartStartY + chartHeight - ((value - minHeight) / (maxHeight - minHeight)) * chartHeight
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = colors[i], style = Stroke(width = if (z == 0.0) 3f else 2f))
        }
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
            textSize = 28f
            color = android.graphics.Color.BLACK
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val lastLMS = referenceData.last()
        val labelX = chartStartX + chartWidth + 10f
        val zScores = listOf(3.0, 2.0, 0.0, -2.0, -3.0)
        val colors = listOf(android.graphics.Color.BLACK, android.graphics.Color.RED, android.graphics.Color.parseColor("#4CAF50"), android.graphics.Color.RED, android.graphics.Color.BLACK)

        zScores.forEachIndexed { index, zScore ->
            val value = calculatePercentileFromLMS(lastLMS.L, lastLMS.M, lastLMS.S, zScore).toFloat()
            val y = chartStartY + chartHeight - ((value - minHeight) / (maxHeight - minHeight)) * chartHeight
            textPaint.color = colors[index]
            drawContext.canvas.nativeCanvas.drawText(if (zScore > 0) "+${zScore.toInt()}" else zScore.toInt().toString(), labelX, y + 8f, textPaint)
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
        val textPaint = Paint().asFrameworkPaint().apply { isAntiAlias = true; textSize = 22f; color = android.graphics.Color.BLACK }
        
        // Ejes
        drawLine(color = Color.Black, start = Offset(chartStartX, chartStartY), end = Offset(chartStartX, chartStartY + chartHeight), strokeWidth = 2f)
        drawLine(color = Color.Black, start = Offset(chartStartX, chartStartY + chartHeight), end = Offset(chartStartX + chartWidth, chartStartY + chartHeight), strokeWidth = 2f)

        // Etiquetas X
        for (month in 0..maxMonths.toInt()) {
            val x = chartStartX + (month / maxMonths) * chartWidth
            drawContext.canvas.nativeCanvas.drawText("$month", x - 10f, chartStartY + chartHeight + 30f, textPaint)
        }

        // Etiquetas Y
        for (value in minHeight.toInt()..maxHeight.toInt() step 5) {
            val y = chartStartY + chartHeight - ((value - minHeight) / (maxHeight - minHeight)) * chartHeight
            drawContext.canvas.nativeCanvas.drawText("$value", chartStartX - 45f, y + 8f, textPaint)
        }
    }

    private fun DrawScope.plotPatientData(
        records: List<GrowthRecord>,
        chartStartX: Float,
        chartStartY: Float,
        chartWidth: Float,
        chartHeight: Float,
        maxMonths: Float,
        minHeight: Float,
        maxHeight: Float
    ) {
        val sortedRecords = records.sortedBy { it.ageInMonths }

        // Línea de tendencia
        if (sortedRecords.size > 1) {
            val linePath = Path()
            sortedRecords.forEachIndexed { index, record ->
                val x = chartStartX + (record.ageInMonths.toFloat() / maxMonths) * chartWidth
                val y = chartStartY + chartHeight - ((record.height.toFloat() - minHeight) / (maxHeight - minHeight)) * chartHeight
                if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
            }
            drawPath(path = linePath, color = Color(0xFF1565C0), style = Stroke(width = 2f))
        }

        // Puntos
        sortedRecords.forEach { record ->
            val x = chartStartX + (record.ageInMonths.toFloat() / maxMonths) * chartWidth
            val y = chartStartY + chartHeight - ((record.height.toFloat() - minHeight) / (maxHeight - minHeight)) * chartHeight

            drawCircle(color = Color(0xFF1565C0).copy(alpha = 0.25f), radius = 10f, center = Offset(x, y))
            drawCircle(color = Color(0xFF1976D2), radius = 6f, center = Offset(x, y))
            drawCircle(color = Color.White, radius = 2.5f, center = Offset(x, y))
        }
    }

    private data class PercentileLine(val zScore: Double, val color: Color, val strokeWidth: Float)
}
