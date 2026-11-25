package com.ys.cunaco.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ys.cunaco.R
import com.ys.cunaco.ui.components.PhdLayoutMenu

@Composable
fun TermsConditions(navController: NavController, openDrawer: () -> Unit) {
    PhdLayoutMenu(
        title = "Terminos y condiciones",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            TermsConditionsContent()
        }
    }
}

@Composable
fun TermsConditionsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // ✅ Enables scrolling
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Términos y Condiciones de Uso de CunaCo app \n",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Fecha de última actualización: 3 de septiembre de 2025 \n",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.legal_terms_text),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify,
        )
    }
}
