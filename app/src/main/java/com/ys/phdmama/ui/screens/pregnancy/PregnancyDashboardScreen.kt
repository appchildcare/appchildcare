package com.ys.phdmama.ui.screens.pregnancy

import PregnancyViewModel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.viewmodel.UserDataViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PregnancyDashboardScreen (
    navController: NavHostController,
    userViewModel: UserDataViewModel = hiltViewModel(),
    pregnancyTrackingViewModel: PregnancyViewModel = hiltViewModel(),
    openDrawer: () -> Unit
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val currentPregnancyTracking by pregnancyTrackingViewModel.currentPregnancyTracking.collectAsState()

    if(currentUser?.role == "waiting") {
        userViewModel.createUserChecklists("waiting")
    }

    if(currentUser?.role == "born") {
        userViewModel.createUserChecklists("born")
    }

    LaunchedEffect(Unit) {
        userViewModel.fetchCurrentUser()
        pregnancyTrackingViewModel.fetchPregnancyTracking()
    }

    PhdLayoutMenu(
        title = "Panel",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_child_care_logo),
                contentDescription = "Logo image",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .height(190.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.mascota_juntos),
                contentDescription = "Pregnant women",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp)
            )

            if (currentUser == null) {
                Text("No se encuentra la data, intente en otro momento...")
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${currentUser?.displayName}, tu fecha de parto aproximada es:",
                    style = MaterialTheme.typography.bodyLarge
                )


                if(currentPregnancyTracking == null) {
                    Text("Cargando...")
                } else {
                    val formattedDate = remember(currentPregnancyTracking?.birthProximateDate) {
                        android.text.format.DateFormat.format("dd MMMM yyyy", currentPregnancyTracking?.birthProximateDate).toString()
                    }
                    Text(text = formattedDate,
                        style = MaterialTheme.typography.titleMedium)

                    val fechaFinal = currentPregnancyTracking?.birthProximateDate
                        ?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

                    val diasEntreFechas = ChronoUnit.DAYS.between(LocalDate.now(), fechaFinal)

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Días para tu fecha de parto",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$diasEntreFechas",
                        style = MaterialTheme.typography.titleMedium
                    )

                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
