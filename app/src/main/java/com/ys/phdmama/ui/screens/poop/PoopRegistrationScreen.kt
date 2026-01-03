package com.ys.phdmama.ui.screens.poop

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ys.phdmama.R
import com.ys.phdmama.model.PoopColor
import com.ys.phdmama.model.PoopSize
import com.ys.phdmama.model.PoopTexture
import com.ys.phdmama.ui.components.PhdLayoutMenu
import com.ys.phdmama.ui.theme.secondaryCream
import com.ys.phdmama.viewmodel.PoopRegistrationViewModel
import java.util.Calendar

@Composable
fun PoopRegistrationScreen(
    navController: NavHostController,
    openDrawer: () -> Unit,
    userId: String,
    babyId: String,
    babyName: String = "Benjamín",
    viewModel: PoopRegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle success state
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "Registro guardado exitosamente", Toast.LENGTH_SHORT).show()
//            viewModel.clearSuccess()
//            onNavigateBack()
        }
    }

    // Handle error state
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    PhdLayoutMenu(
        title = "Registro Cacas",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
//                .background(
//                    Color.White,
//                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
//                )
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Time Selection
            TimeSection(
                selectedTime = uiState.selectedTime,
                onTimeSelected = viewModel::updateTime
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Color Selection
            ColorSection(
                selectedColor = uiState.selectedColor,
                onColorSelected = viewModel::updateColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Texture Selection
            TextureSection(
                selectedTexture = uiState.selectedTexture,
                onTextureSelected = viewModel::updateTexture
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Size Selection
            SizeSection(
                selectedSize = uiState.selectedSize,
                onSizeSelected = viewModel::updateSize
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Notes Section
            NotesSection(
                notes = uiState.notes,
                onNotesChanged = viewModel::updateNotes
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Save Button
            Button(
                onClick = {
                    viewModel.savePoopRecord(userId, babyId)
                    // viewModel.updateQuestion(updated)
                          },
                enabled = !uiState.isLoading && uiState.isValid(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7DD3C0),
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Aceptar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

    }

//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFF7DD3C0))
//    ) {
//        // Content
//
//    }
}



@Composable
private fun BabyInfoSection(babyName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFF7DD3C0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = babyName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun TimeSection(
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current

    Column {
        SectionTitle("Hora")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val calendar = Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val time = String.format("%02d:%02d", hourOfDay, minute)
                        onTimeSelected(time)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            border = BorderStroke(1.dp, Color(0xFF7DD3C0)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White
            )
        ) {
            Text(
                text = selectedTime.ifEmpty { "12:40" },
                fontSize = 16.sp,
                color = if (selectedTime.isEmpty()) Color.Gray else Color.Black
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF7DD3C0)
            )
        }
    }
}

@Composable
private fun ColorSection(
    selectedColor: PoopColor?,
    onColorSelected: (PoopColor) -> Unit
) {
    Column {
        SectionTitle("Color")

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(PoopColor.values()) { color ->
                ColorOption(
                    color = color,
                    isSelected = selectedColor == color,
                    onSelected = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: PoopColor,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val colorValue = when (color) {
        PoopColor.MUY_OSCURO -> Color(0xFF333333)
        PoopColor.VERDE_OSCURO -> Color(0xFF4A7C59)
        PoopColor.MARRON -> Color(0xFF8B4513)
        PoopColor.AMARILLO -> Color(0xFFFFD700)
        PoopColor.OTROS -> Color(0xFFFFA500)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onSelected() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(colorValue, CircleShape)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = Color(0xFF7DD3C0),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = color.displayName,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = if (isSelected) Color(0xFF7DD3C0) else Color.Gray
        )
    }
}

@Composable
private fun TextureSection(
    selectedTexture: PoopTexture?,
    onTextureSelected: (PoopTexture) -> Unit
) {
    Column {
        SectionTitle("Textura")

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(PoopTexture.values()) { texture ->
                TextureOption(
                    texture = texture,
                    isSelected = selectedTexture == texture,
                    onSelected = { onTextureSelected(texture) }
                )
            }
        }
    }
}

@Composable
private fun TextureOption(
    texture: PoopTexture,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onSelected() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isSelected) Color(0xFF7DD3C0) else secondaryCream,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Option 1: Using Image with drawable resource
            when (texture) {
                PoopTexture.LIQUIDA -> {
                    Image(
                        painter = painterResource(id = R.mipmap.caca_liquida),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
//                        colorFilter = if (isSelected) ColorFilter.tint(Color.White) else ColorFilter.tint(Color.Gray)
                    )
                }
                PoopTexture.PASTOSA -> {
                    Image(
                        painter = painterResource(id = R.mipmap.caca_pastosa),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
//                        colorFilter = if (isSelected) ColorFilter.tint(Color.White) else ColorFilter.tint(Color.Gray)
                    )
                }
                PoopTexture.DURA -> {
                    Image(
                        painter = painterResource(id = R.mipmap.caca_dura),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
//                        colorFilter = if (isSelected) ColorFilter.tint(Color.White) else ColorFilter.tint(Color.Gray)
                    )
                }
                PoopTexture.CON_MOCOS -> {
                    Image(
                        painter = painterResource(id = R.mipmap.caca_moco),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
//                        colorFilter = if (isSelected) ColorFilter.tint(Color.White) else ColorFilter.tint(Color.Gray)
                    )
                }
                PoopTexture.CON_SANGRE -> {
                    Image(
                        painter = painterResource(id = R.mipmap.caca_sangre),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
//                        colorFilter = if (isSelected) ColorFilter.tint(Color.White) else ColorFilter.tint(Color.Gray)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = texture.displayName,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = if (isSelected) Color(0xFF7DD3C0) else Color.Gray
        )
    }
}

@Composable
private fun SizeSection(
    selectedSize: PoopSize?,
    onSizeSelected: (PoopSize) -> Unit
) {
    Column {
        SectionTitle("Tamaño")

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(PoopSize.values()) { size ->
                SizeOption(
                    size = size,
                    isSelected = selectedSize == size,
                    onSelected = { onSizeSelected(size) }
                )
            }
        }
    }
}

@Composable
private fun SizeOption(
    size: PoopSize,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onSelected() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isSelected) Color(0xFF7DD3C0) else secondaryCream,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when (size) {
                PoopSize.MONEDA -> {
                    Image(
                        painter = painterResource(id = R.mipmap.caca_tamano_moneda),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
                PoopSize.CUCHARA_SOPERA -> {
                    Image(
                        painter = painterResource(id = R.mipmap.caca_tamano_moneda),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
                PoopSize.MAS_GRANDE -> {
                    Image(
                        painter = painterResource(id = R.mipmap.caca_tamano_moneda),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = size.displayName,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = if (isSelected) Color(0xFF7DD3C0) else Color.Gray
        )
    }
}

@Composable
private fun NotesSection(
    notes: String,
    onNotesChanged: (String) -> Unit
) {
    Column {
        SectionTitle("Notas")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChanged,
            placeholder = {
                Text(
                    text = "Escribe aquí más sobre la caca de tu bebé",
                    color = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7DD3C0),
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.width(8.dp))

    }
}
