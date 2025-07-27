package com.ys.phdmama.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ys.phdmama.R

val PhdMamaFontHelvetica = FontFamily(
    Font(R.font.helvetica_rounded_bold, FontWeight.Bold)
)

val PhdMamaFontQuickSandMedium = FontFamily(
    Font(R.font.quicksand_medium, FontWeight.Medium)
)

val PhdMamaFontQuickRegular = FontFamily(
    Font(R.font.quicksand_regular, FontWeight.Normal)
)

// Set of Material typography styles to start with
val Typography = Typography(
    titleLarge = TextStyle( // Titulo principal
        fontFamily = PhdMamaFontHelvetica,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = primaryGray
    ),
    titleMedium = TextStyle( // Subtitulo
        fontFamily = PhdMamaFontHelvetica,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = primaryGray
        ),
    bodyLarge = TextStyle( // Texto Parrafo
        fontFamily = PhdMamaFontQuickRegular,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = primaryGray
    ),
    bodyMedium = TextStyle( // Texto Parrafo corto
        fontFamily = PhdMamaFontQuickSandMedium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = primaryGray
    ),
    bodySmall = TextStyle( // Texto notas
        fontFamily = PhdMamaFontQuickSandMedium,
        fontSize = 12.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = primaryGray
    ),
    labelLarge = TextStyle( // Labels notas 16px
        fontFamily = PhdMamaFontHelvetica,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = primaryGray
    ),
    labelSmall = TextStyle(
        fontFamily = PhdMamaFontHelvetica, // Labels notas 14px
        fontSize = 14.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = primaryGray
    ),
)