package com.ys.phdmama.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun PhdMediumText(text: String){
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
}

@Composable
fun PhdNormalText(text: String){
    Text(text, style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun PhdBoldText(text: String){
    Text(text, style = MaterialTheme.typography.labelSmall)
}

@Composable
fun PhdLabelText(text: String){
    Text(text, style = MaterialTheme.typography.labelLarge)
}

@Composable
fun PhdErrorText(text: String){
    Text(text, style = MaterialTheme.typography.labelSmall, color = Color.Red)
}