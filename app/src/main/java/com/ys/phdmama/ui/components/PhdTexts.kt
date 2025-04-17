package com.ys.phdmama.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun PhdMediumText(text: String){
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
}

@Composable
fun PhdNormalText(text: String){
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Normal)
}