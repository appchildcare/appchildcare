package com.ys.cunaco.ui.screens.emergency

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ys.cunaco.ui.components.PhdLayoutMenu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ---------------------------------------------------------------------------
// 1. VIEWMODEL + MODELOS DE DATOS
// ---------------------------------------------------------------------------

class CheckItemsViewModel : ViewModel() {

    data class SymptomItem(
        val id: String,
        val text: String,
        val checked: Boolean = false
    )

    data class SubtopicGroup(
        val subtopic: String,
        val items: List<SymptomItem>
    )

    data class TopicGroup(
        val topic: String,
        val months: Int, // -1 = no mostrar badge de meses
        val subtopics: List<SubtopicGroup>
    )

    private val _topicGroups = MutableStateFlow(buildInitialData())
    val topicGroups: StateFlow<List<TopicGroup>> = _topicGroups.asStateFlow()

    fun onItemCheckedChange(itemId: String, checked: Boolean) {
        _topicGroups.update { groups ->
            groups.map { topic ->
                topic.copy(
                    subtopics = topic.subtopics.map { sub ->
                        sub.copy(
                            items = sub.items.map { item ->
                                if (item.id == itemId) item.copy(checked = checked) else item
                            }
                        )
                    }
                )
            }
        }
    }

    private fun items(prefix: String, vararg texts: String): List<SymptomItem> =
        texts.mapIndexed { i, t -> SymptomItem(id = "$prefix-$i", text = t) }

    private fun buildInitialData(): List<TopicGroup> = listOf(
        TopicGroup(
            topic = "Menos de 6 meses",
            months = 6,
            subtopics = listOf(
                SubtopicGroup(
                    "Fiebre o Temperatura Anormal",
                    items(
                        "m6-fiebre",
                        "Tiene fiebre de 38°C o más. En un bebé menor de 3 meses, la fiebre es siempre una urgencia.",
                        "Su temperatura es inferior a 35.5°C."
                    )
                ),
                SubtopicGroup(
                    "Cambios en su Comportamiento",
                    items(
                        "m6-comport",
                        "Está demasiado dormido o es muy difícil despertarlo.",
                        "Está extremadamente irritable y no hay forma de calmarlo.",
                        "Su llanto es débil, agudo o inusual."
                    )
                ),
                SubtopicGroup(
                    "Problemas con la Alimentación",
                    items(
                        "m6-alimentacion",
                        "Rechaza la comida de forma persistente o repetida.",
                        "No tiene fuerza para succionar o se cansa mucho al comer."
                    )
                ),
                SubtopicGroup(
                    "Dificultad para Respirar",
                    items(
                        "m6-respirar",
                        "Respira muy rápido.",
                        "Hace un ruido como un quejido al exhalar.",
                        "Se le hunden las costillas o el hueco del cuello al respirar.",
                        "Sus labios, lengua o piel tienen un tono azulado.",
                        "Hace pausas al respirar."
                    )
                ),
                SubtopicGroup(
                    "Vómitos o Problemas Digestivos",
                    items(
                        "m6-vomitos",
                        "Vomita repetidamente y con fuerza.",
                        "El vómito es de color verde o contiene sangre.",
                        "Tiene la barriga muy hinchada y dura."
                    )
                ),
                SubtopicGroup(
                    "Mal Aspecto o Mala Circulación/Deshidratación",
                    items(
                        "m6-deshidratacion",
                        "Está muy pálido o su piel tiene un aspecto moteado, como de mármol.",
                        "Sus manos y pies están fríos.",
                        "Al presionar la uña, el color rosado tarda más de 3 segundos en volver.",
                        "Disminución significativa de la cantidad de orina que realiza (menos pañales mojados al día).",
                        "Llanto sin lágrima.",
                        "Mucosas secas."
                    )
                ),
                SubtopicGroup(
                    "Signos Neurológicos",
                    items(
                        "m6-neurologico",
                        "Presenta cualquier tipo de convulsión (movimientos rítmicos, sacudidas, mirada fija).",
                        "La parte blanda de su cabeza (fontanela) se siente hinchada o tensa cuando está tranquilo.",
                        "Su cuerpo se pone repentinamente flácido o rígido."
                    )
                ),
                SubtopicGroup(
                    "Piel, Ombligo y Sangrado",
                    items(
                        "m6-piel",
                        "Signos de infección en el ombligo: piel roja, hinchada, caliente, con pus o mal olor alrededor del cordón umbilical.",
                        "Le aparecen puntitos rojos pequeños que no desaparecen al estirar la piel o moretones sin haberse golpeado.",
                        "Cualquier sangrado inexplicable."
                    )
                )
            )
        ),
        TopicGroup(
            topic = "Más de 6 meses en adelante",
            months = -1,
            subtopics = listOf(
                SubtopicGroup(
                    "Dificultad para Respirar",
                    items(
                        "m6mas-respirar",
                        "Respira muy agitado, le \"silba\" el pecho o hace ruidos roncos al inspirar.",
                        "Se le marcan las costillas o el abdomen se mueve mucho al respirar.",
                        "No puede hablar o llorar por la falta de aire.",
                        "Sus labios o cara se ven azulados."
                    )
                ),
                SubtopicGroup(
                    "Alteración de la Conciencia o Comportamiento",
                    items(
                        "m6mas-conciencia",
                        "Está confundido, desorientado o no te reconoce.",
                        "Está muy adormilado y es difícil mantenerlo despierto.",
                        "Ha perdido el conocimiento, aunque sea por un instante."
                    )
                ),
                SubtopicGroup(
                    "Convulsiones o Síntomas Neurológicos",
                    items(
                        "m6mas-neurologico",
                        "Tiene una convulsión (movimientos incontrolables, sacudidas), especialmente si es la primera vez o está asociada a fiebre alta.",
                        "Se queja de un dolor de cabeza muy intenso y repentino, con vómitos, rigidez de cuello o molestia extrema a la luz.",
                        "Presenta debilidad en una parte del cuerpo, dificultad para caminar o para hablar."
                    )
                ),
                SubtopicGroup(
                    "Traumatismos o Golpes Fuertes",
                    items(
                        "m6mas-trauma",
                        "Un golpe importante en la cabeza, sobre todo si hay pérdida de conocimiento, vómitos repetidos, comportamiento extraño o líquido claro por nariz u oídos.",
                        "Cualquier golpe que provoque una deformidad evidente.",
                        "No puede mover alguna extremidad por el dolor.",
                        "Una herida con sangrado abundante."
                    )
                ),
                SubtopicGroup(
                    "Dolor Intenso",
                    items(
                        "m6mas-dolor",
                        "Un dolor muy fuerte que no se calma con los analgésicos que usas habitualmente."
                    )
                ),
                SubtopicGroup(
                    "Vómitos y Deshidratación",
                    items(
                        "m6mas-vomitos",
                        "Vomita sin parar y no tolera líquidos.",
                        "Muestra signos de deshidratación: boca seca, llanto sin lágrimas, orina muy poco o nada en varias horas, muy decaído y somnoliento."
                    )
                ),
                SubtopicGroup(
                    "Sangrados o Manchas Raras",
                    items(
                        "m6mas-sangrado",
                        "Aparición repentina de moretones sin una causa clara.",
                        "Pequeños puntos rojos en la piel que se extienden rápidamente.",
                        "Sangrado por la nariz, encías u otro lugar que no se detiene."
                    )
                ),
                SubtopicGroup(
                    "Salud Mental (en niños mayores y adolescentes)",
                    items(
                        "m6mas-saludmental",
                        "Expresa ideas o intenciones de hacerse daño a sí mismo o a otros.",
                        "Presenta un cambio de comportamiento tan severo que es incapaz de comer, dormir o realizar sus actividades básicas."
                    )
                )
            )
        )
    )
}

// ---------------------------------------------------------------------------
// 2. PANTALLA PRINCIPAL
// ---------------------------------------------------------------------------

@Composable
fun EmergencyScreen(
    viewModel: CheckItemsViewModel = viewModel(),
    navController: NavHostController,
    openDrawer: () -> Unit
) {
    val topicGroups by viewModel.topicGroups.collectAsState()

    PhdLayoutMenu(
        title = "Emergencia",
        navController = navController,
        openDrawer = openDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    IntroMessage()
                }

                topicGroups.forEach { topicGroup ->
                    item(key = "topic-${topicGroup.topic}") {
                        TopicSection(
                            topicGroup = topicGroup,
                            onItemCheckedChange = viewModel::onItemCheckedChange
                        )
                    }

                    items(
                        items = topicGroup.subtopics,
                        key = { "sub-${topicGroup.topic}-${it.subtopic}" }
                    ) { subtopicGroup ->
                        ExpandableListItem(
                            subtopicGroup = subtopicGroup,
                            onItemCheckedChange = viewModel::onItemCheckedChange
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun IntroMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Confía en tu instinto. Nadie conoce a tu hijo mejor que tú.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "• Si sientes que algo no va bien, consulta con un médico lo más pronto.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "• Este listado no reemplaza la valoración de un médico.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 3. TOPIC SECTION (título de cada grupo de edad)
// ---------------------------------------------------------------------------

@Composable
fun TopicSection(
    topicGroup: CheckItemsViewModel.TopicGroup,
    onItemCheckedChange: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = topicGroup.topic,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (topicGroup.months >= 0) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "${topicGroup.months} meses",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Months",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. EXPANDABLE LIST ITEM (subtema con checklist de síntomas)
// ---------------------------------------------------------------------------

@Composable
fun ExpandableListItem(
    subtopicGroup: CheckItemsViewModel.SubtopicGroup,
    onItemCheckedChange: (String, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header - Subtopic (clickable to expand/collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subtopicGroup.subtopic,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.ArrowDropDown
                    },
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            // Expandable content - List of items
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    subtopicGroup.items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemCheckedChange(item.id, !item.checked)
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
//                            Checkbox(
//                                checked = item.checked,
//                                onCheckedChange = { checked ->
//                                    onItemCheckedChange(item.id, checked)
//                                },
//                                modifier = Modifier.padding(top = 2.dp)
//                            )
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 12.dp, start = 4.dp)
                            )
                        }

                        if (index != subtopicGroup.items.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}