package com.ys.cunaco.ui.screens.poop

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ys.cunaco.R
import com.ys.cunaco.model.PoopColor
import com.ys.cunaco.model.PoopSize
import com.ys.cunaco.model.PoopTexture

@Composable
fun PoopColor.label(): String = when (this) {
    PoopColor.MUY_OSCURO -> stringResource(R.string.poop_color_very_dark)
    PoopColor.VERDE_OSCURO -> stringResource(R.string.poop_color_dark_green)
    PoopColor.MARRON -> stringResource(R.string.poop_color_brown)
    PoopColor.AMARILLO -> stringResource(R.string.poop_color_yellow)
    PoopColor.OTROS -> stringResource(R.string.poop_color_other)
}

@Composable
fun PoopTexture.label(): String = when (this) {
    PoopTexture.LIQUIDA -> stringResource(R.string.poop_texture_liquid)
    PoopTexture.PASTOSA -> stringResource(R.string.poop_texture_pasty)
    PoopTexture.DURA -> stringResource(R.string.poop_texture_hard)
    PoopTexture.CON_MOCOS -> stringResource(R.string.poop_texture_mucus)
    PoopTexture.CON_SANGRE -> stringResource(R.string.poop_texture_blood)
}

@Composable
fun PoopSize.label(): String = when (this) {
    PoopSize.POCO -> stringResource(R.string.poop_size_small)
    PoopSize.MITAD_PANAL -> stringResource(R.string.poop_size_half_diaper)
    PoopSize.PANAL_DESBORDADO -> stringResource(R.string.poop_size_overflowing)
}

// Translate a stored string (either `value` or `displayName`) for display.
// If it doesn't match any entry, show the stored text as is.
@Composable
fun localizedPoopColor(stored: String): String =
    PoopColor.values().firstOrNull {
        it.value.equals(stored, ignoreCase = true) || it.displayName.equals(stored, ignoreCase = true)
    }?.label() ?: stored

@Composable
fun localizedPoopTexture(stored: String): String =
    PoopTexture.values().firstOrNull {
        it.value.equals(stored, ignoreCase = true) || it.displayName.equals(stored, ignoreCase = true)
    }?.label() ?: stored

@Composable
fun localizedPoopSize(stored: String): String =
    PoopSize.values().firstOrNull {
        it.value.equals(stored, ignoreCase = true) || it.displayName.equals(stored, ignoreCase = true)
    }?.label() ?: stored