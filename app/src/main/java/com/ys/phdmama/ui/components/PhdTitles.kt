package com.ys.phdmama.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun PhdSubtitle(subtitle: String){
    Text(subtitle, fontSize = 24.sp, fontWeight = FontWeight.Bold)
}