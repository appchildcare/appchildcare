package com.ys.phdmama.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun PhdTitle(subtitle: String){
    Text(subtitle, fontSize = 28.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun PhdSubtitle(subtitle: String){
    Text(subtitle, style = MaterialTheme.typography.titleMedium)
}