package com.ys.cunaco.util
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {
    fun renderDate(): String {
        //TODO: pass languague dinamically according device language
        val formatter = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("es", "EC"))
        val currentDate = formatter.format(Date())
            .replaceFirstChar {
                // Solo capitaliza la primera letra del mes, no del texto completo
                val parts = it.toString()
                it
            }

    // Capitalizar únicamente el mes
        val formattedDate = currentDate.split(" ").toMutableList().apply {
            this[1] = this[1].replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase(Locale("es", "EC")) else c.toString()
            }
        }.joinToString(" ")
        return formattedDate
    }
}