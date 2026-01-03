package com.ys.phdmama.ui.screens.waiting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ys.phdmama.ui.components.PhdLayoutMenu

@Composable
fun GynecologistScreen(navController: NavController = rememberNavController(),
                       openDrawer: () -> Unit){
    var modifier: Modifier = Modifier


    PhdLayoutMenu(
        title = "Preguntas al ginecólogo",
        navController = navController,
        openDrawer = openDrawer
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¿Cuándo fue su última menstruación?",
//                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = "",
                    onValueChange = {
//                    answer = it
//                    registerViewModel.updateUserField("displayName", it)
                    },
                    singleLine = true,
                    modifier = modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¿Tiene alguna pregunta o inquietud sobre su embarazo?",
//                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge
                )


                OutlinedTextField(
                    value = "",
                    onValueChange = {
//                    answer = it
//                    registerViewModel.updateUserField("displayName", it)
                    },
//                    label = { Text("Re") },
                    singleLine = true,
                    modifier = modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

    }
}