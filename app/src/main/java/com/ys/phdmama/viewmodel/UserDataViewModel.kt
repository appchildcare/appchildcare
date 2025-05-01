package com.ys.phdmama.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class User(
    @PropertyName("displayName") val displayName: String = "",
    @PropertyName("ecoWeeks") val ecoWeeks: Long = 0,
    @PropertyName("birthProximateDate") val birthProximateDate: Date? = null,
    @PropertyName("role") val role: String = ""
)

data class ChecklistItem(
    val id: Int,
    val topic: String = "",
    val text: String,
    var checked: Boolean,
)


class UserDataViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> get() = _currentUser

    init {
        Log.d("FirebaseInit", "Firestore instance created")
    }

    fun fetchCurrentUser() {
        viewModelScope.launch {
            // Get the current user's UID
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("FirestoreError", "No user is currently signed in")
                return@launch
            }
            firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d("FirestoreSuccess", "Fetched current user data")
                        val user = document.toObject(User::class.java)
                        _currentUser.value = user
                    } else {
                        Log.e("FirestoreError", "No user document found for UID: $currentUserId")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreError", "Error fetching users", exception)
                }
        }
    }

    fun createUserChecklists(userRole: String) {
        val currentUserId = auth.currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        val userRef = currentUserId?.let { db.collection("users").document(it) }

        if (userRef != null) {
            userRef.collection("checklists").get()
                .addOnSuccessListener { documents ->
                    val checklistNames = documents.documents.map { it.id } // Get existing checklist names

                    when (userRole) {
                        "waiting" -> {
                            if (!checklistNames.contains("waiting")) { // Create "waiting" checklist only if it doesn't exist
                                val waitingChecklist = mapOf(
                                    "1" to ChecklistItem(1, "Documentación Esencial","Documento de identidad original de la madre y del padre (si aplica)", false, ),
                                    "2" to ChecklistItem(2, "Documentación Esencial","Copias de los documentos de identidad. (Guardar en un lugar separado del original).", false),
                                    "3" to ChecklistItem(3, "Documentación Esencial", "Tarjeta o información del seguro médico. (Verificar cobertura para el parto y el recién nacido).", false),
                                    "4" to ChecklistItem(4, "Documentación Esencial", "Autorización o carta de admisión del hospital/clínica. (Si la proporcionaron previamente).", false, ),
                                    "5" to ChecklistItem(5, "Documentación Esencial", "Plan de parto. (Si lo has elaborado, tener varias copias a mano).", false),
                                    "6" to ChecklistItem(6, "Documentación Esencial", "Información de contacto de familiares o amigos que te apoyarán.", false),

                                    "7" to ChecklistItem(7, "Aspectos Laborales y Legales", "Documentación del permiso de maternidad/paternidad. (Asegurarse de tener los formularios necesarios y conocer los plazos).", false,),
                                    "8" to ChecklistItem(8, "Aspectos Laborales y Legales", "Información sobre vacaciones acumuladas. (Coordinar con el empleador).", false,),
                                    "9" to ChecklistItem(9, "Aspectos Laborales y Legales", "Cualquier otro documento relevante de tu empresa o sindicato.", false),

                                    "10" to ChecklistItem(10, "Preparación y Conocimiento del Entorno:", "Visita guiada al hospital o clínica donde nacerá tu bebé. (Familiarizarse con las instalaciones, la ruta, los procedimientos).", false),
                                    "11" to ChecklistItem(11, "Preparación y Conocimiento del Entorno:", "Consulta prenatal con el pediatra. (Idealmente, conocer al profesional que atenderá a tu bebé. Preguntar sobre sus protocolos y resolver dudas iniciales sobre lactancia, cuidados del recién nacido, etc.).", false),
                                    "12" to ChecklistItem(12, "Preparación y Conocimiento del Entorno:", "Confirmar el plan de traslado al hospital/clínica. (Ruta, transporte disponible a cualquier hora).", false),
                                    "13" to ChecklistItem(13, "Preparación y Conocimiento del Entorno:", "Tener lista la bolsa de hospital (ver sección III). (Idealmente, preparada varias semanas antes de la fecha probable de parto).", false),

                                    "14" to ChecklistItem(14, "Aspectos Financieros:", "Tener efectivo y tarjetas de crédito disponibles.", false),
                                    "15" to ChecklistItem(15, "Aspectos Financieros:", "Informarse sobre los costos estimados del parto y la estadía.", false),
                                    "16" to ChecklistItem(16, "Aspectos Financieros:", "Considerar un plan de ahorro o seguro para los gastos del bebé.", false),
                                )
                                userRef.collection("checklists").document("waiting").set(waitingChecklist)
                                    .addOnSuccessListener { Log.d("Firestore", "Waiting checklist created") }
                                    .addOnFailureListener { e -> Log.e("Checklist", "Error creating waiting checklist", e) }
                            } else {
                                Log.d("Firestore", "Waiting checklist already exists, skipping creation")
                            }
                        }

                        "born" -> {
                            if (!checklistNames.contains("born")) { // Create "born" checklist only if it doesn't exist
                                val bornChecklist = mapOf(
                                    "4" to ChecklistItem(4, "", "Alcohol en spray (para quien cargue al bebé, prefiere siempre el lavado de manos).", false),
                                    "5" to ChecklistItem(5, "", "Pañales para recién nacido", false),
                                    "6" to ChecklistItem(6, "", "Toallitas húmedas para recién nacido, en agua.", false),
                                    "7" to ChecklistItem(7, "", "Lima de uñas (de preferencia de vidrio).", false),
                                    "8" to ChecklistItem(8, "", "3 Conjuntos de mangas largas: Botones delanteros.", false),
                                    "9" to ChecklistItem(9, "", "2 Mantas.", false),
                                    "10" to ChecklistItem(10, "", "3 gorritos.", false),
                                    "11" to ChecklistItem(11, "", "Cambiador (protector).", false),
                                    "12" to ChecklistItem(12, "", "Asiento del bebé para vehículo.", false),
                                    "13" to ChecklistItem(13, "", "Regalo para herman@ o sobrin@.", false),

                                )
                                userRef.collection("checklists").document("born").set(bornChecklist)
                                    .addOnSuccessListener { Log.d("Firestore", "Born checklist created") }
                                    .addOnFailureListener { e -> Log.e("Checklist", "Error creating born checklist", e) }
                            } else {
                                if (!checklistNames.contains("born_leave_home")) {
                                    val leaveHomeChecklist = mapOf(
                                        "1" to ChecklistItem(1, "Consulta pediátrica", "Revisar el calendario de vacunación y administrar las necesarias o recomendadas para el destino.", false),
                                        "2" to ChecklistItem(2, "Consulta pediátrica", "Obtener recomendaciones específicas de salud para el lugar de destino (mal de altura, enfermedades transmitidas por mosquitos, etc.).", false),
                                        "3" to ChecklistItem(3, "Consulta pediátrica", "Solicitar un informe médico resumido del niño/bebé, especialmente si tiene alguna condición médica preexistente.", false),
                                        "4" to ChecklistItem(4, "Consulta pediátrica", "Preguntar al pediatra sobre medicamentos básicos de venta libre que podrían ser útiles (fiebre, dolor, mareos).", false),
                                        "5" to ChecklistItem(5, "Documentación", "Pasaportes y visados (si son necesarios) con suficiente validez.", false),
                                        "6" to ChecklistItem(6, "Documentación", "Copias de los documentos importantes (pasaportes, billetes, reservas) guardadas en un lugar separado y digitalmente.", false),
                                        "7" to ChecklistItem(7, "Documentación", "Seguro de viaje con cobertura médica para todos los miembros de la familia.", false),
                                        "8" to ChecklistItem(8, "Documentación", "Cartilla de vacunación o certificado de salud del niño/bebé.", false),
                                        "9" to ChecklistItem(9, "Documentación", "Consentimiento informado firmado por ambos padres (si viaja solo uno de ellos o un tutor).", false),
                                        "10" to ChecklistItem(10, "Planificación del viaje", "Elegir un destino y alojamiento child-friendly (con cunas, tronas, áreas de juego, etc. si es necesario).", false),
                                        "11" to ChecklistItem(11, "Planificación del viaje", "Planificar los traslados teniendo en cuenta las necesidades de los niños (tiempos de espera, comodidad, seguridad).", false),
                                        "12" to ChecklistItem(12, "Planificación del viaje", "Investigar sobre centros de salud y hospitales cercanos al lugar de destino.", false),
                                        "13" to ChecklistItem(13, "Planificación del viaje", "Considerar actividades y horarios que se adapten a las rutinas y edades de los niños.", false),
                                        "14" to ChecklistItem(14, "Botiquín de viaje", "Termómetro digital.", false),
                                        "15" to ChecklistItem(15, "Botiquín de viaje", "Analgésico y antipirético infantil (paracetamol, ibuprofeno) con la dosis adecuada para la edad y peso.", false),
                                        "16" to ChecklistItem(16, "Botiquín de viaje", "Suero oral para la deshidratación.", false),
                                        "17" to ChecklistItem(17, "Botiquín de viaje", "Antiséptico para heridas.", false),
                                        "18" to ChecklistItem(18, "Botiquín de viaje", "Tiritas y gasas.", false),
                                        "19" to ChecklistItem(19, "Botiquín de viaje", "Crema para quemaduras solares con alto factor de protección.", false),
                                        "20" to ChecklistItem(20, "Botiquín de viaje", "Repelente de mosquitos infantil (adecuado para la edad).", false),
                                        "21" to ChecklistItem(21, "Botiquín de viaje", "Antihistamínico (si el pediatra lo recomienda).", false),
                                        "22" to ChecklistItem(22, "Botiquín de viaje", "Medicamentos específicos si el niño/bebé tiene alguna condición médica (asma, alergias, etc.) con receta médica si es necesario.", false),
                                        "23" to ChecklistItem(23, "Botiquín de viaje", "Crema para picaduras.", false),
                                        "24" to ChecklistItem(24, "Botiquín de viaje", "Protector labial con SPF.", false),
                                        "25" to ChecklistItem(25, "Equipaje inteligente", "Ropa cómoda y adecuada para el clima del destino, incluyendo capas.", false),
                                        "26" to ChecklistItem(26, "Equipaje inteligente", "Calzado cómodo para caminar.", false),
                                        "27" to ChecklistItem(27, "Equipaje inteligente", "Sombrero o gorra para proteger del sol.", false),
                                        "28" to ChecklistItem(28, "Equipaje inteligente", "Traje de baño y protección solar (gafas de sol incluidas).", false),
                                        "29" to ChecklistItem(29, "Equipaje inteligente", "Pañales suficientes (considerar la disponibilidad en el destino).", false),
                                        "30" to ChecklistItem(30, "Equipaje inteligente", "Toallitas húmedas.", false),
                                        "31" to ChecklistItem(31, "Equipaje inteligente", "Bolsas para pañales sucios.", false),
                                        "32" to ChecklistItem(32, "Equipaje inteligente", "Baberos.", false),
                                        "33" to ChecklistItem(33, "Equipaje inteligente", "Mantita o objeto de apego para el bebé/niño pequeño.", false),
                                        "34" to ChecklistItem(34, "Equipaje inteligente", "Juguetes pequeños y libros para entretener durante los traslados y tiempos de espera.", false),
                                        "35" to ChecklistItem(35, "Equipaje inteligente", "Dispositivos electrónicos con juegos o películas (con auriculares).", false),
                                        "36" to ChecklistItem(36, "Equipaje inteligente", "Cargadores y adaptadores de corriente si son necesarios.", false),
                                        "37" to ChecklistItem(37, "Equipaje inteligente", "Mochila o bolsa cómoda para llevar las cosas del bebé/niño durante las excursiones.", false),
                                        "38" to ChecklistItem(38, "Equipaje inteligente", "Portabebés ergonómico o cochecito ligero y plegable (según la edad del niño y el tipo de viaje).", false),
                                        "39" to ChecklistItem(39, "Equipaje inteligente", "Alimentos no perecederos y snacks saludables para los traslados.", false),
                                        "40" to ChecklistItem(40, "Equipaje inteligente", "Biberones, tetinas, leche de fórmula (si es necesario) y utensilios de limpieza.", false),
                                        "41" to ChecklistItem(41, "Equipaje inteligente", "Vasos o botellas de agua reutilizables.", false),
                                        "42" to ChecklistItem(42, "Durante el Viaje: Adaptación y Flexibilidad", "Mantener la hidratación ofreciendo líquidos con frecuencia.", false),
                                        "43" to ChecklistItem(43, "Durante el Viaje: Adaptación y Flexibilidad", "Ofrecer comidas y snacks saludables a intervalos regulares.", false),
                                        "44" to ChecklistItem(44, "Durante el Viaje: Adaptación y Flexibilidad", "Respetar las horas de sueño y siesta del niño/bebé en la medida de lo posible.", false),
                                        "45" to ChecklistItem(45, "Durante el Viaje: Adaptación y Flexibilidad", "Crear un ambiente tranquilo y seguro para dormir en lugares desconocidos.", false),
                                        "46" to ChecklistItem(46, "Durante el Viaje: Adaptación y Flexibilidad", "Proteger la piel del sol con ropa adecuada, sombrero y crema solar.", false),
                                        "47" to ChecklistItem(47, "Durante el Viaje: Adaptación y Flexibilidad", "Evitar la exposición al sol en las horas centrales del día.", false),
                                        "48" to ChecklistItem(48, "Durante el Viaje: Adaptación y Flexibilidad", "Vigilar la temperatura corporal del niño/bebé para prevenir el golpe de calor.", false),
                                        "49" to ChecklistItem(49, "Durante el Viaje: Adaptación y Flexibilidad", "Ofrecer actividades que se adapten a su edad e intereses.", false),
                                        "50" to ChecklistItem(50, "Durante el Viaje: Adaptación y Flexibilidad", "Ser flexible con los planes y adaptarse al ritmo de los niños.", false),
                                        "51" to ChecklistItem(51, "Durante el Viaje: Adaptación y Flexibilidad", "Estar atento a cualquier signo de malestar o enfermedad.", false),
                                        "52" to ChecklistItem(52, "Durante el Viaje: Adaptación y Flexibilidad", "Conocer la ubicación del centro de salud más cercano.", false),
                                        "53" to ChecklistItem(53, "Durante el Viaje: Adaptación y Flexibilidad", "Mantener la calma ante situaciones inesperadas.", false),
                                        "54" to ChecklistItem(54, "Al Regresar: Transición Suave", "Observar si aparecen síntomas de alguna enfermedad durante los días posteriores al viaje.", false),
                                        "55" to ChecklistItem(55, "Al Regresar: Transición Suave", "Consultar al pediatra si surge cualquier preocupación de salud.", false),
                                        "56" to ChecklistItem(56, "Al Regresar: Transición Suave", "Permitir que el niño/bebé se readapte gradualmente a la rutina habitual", false),

                                        )
                                    userRef.collection("checklists").document("born_leave_home").set(leaveHomeChecklist)
                                        .addOnSuccessListener { Log.d("Firestore", "Born checklist created") }
                                        .addOnFailureListener { e -> Log.e("Checklist", "Error creating born checklist", e) }
                                }
                                    Log.d("Firestore", "Born checklist already exists, skipping creation")
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error checking existing checklists", e)
                }
        }
    }

    fun fetchWaitingChecklist(userRole: String, onResult: (List<ChecklistItem>) -> Unit) {
        val checklistType = if (userRole == "waiting")  "waiting" else "born"
        val userId = auth.currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        val docRef = userId?.let {
            db.collection("users").document(it)
                .collection("checklists").document(checklistType)
        }

        if (docRef != null) {
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val checklistItems = mutableListOf<ChecklistItem>()

                        document.data?.forEach { (_, value) ->
                            if (value is Map<*, *>) {
                                val item = ChecklistItem(
                                    id = (value["id"] as? Long)?.toInt() ?: 0,
                                    text = value["text"] as? String ?: "",
                                    checked = when (value["checked"]) {
                                        is Boolean -> value["checked"] as Boolean
                                        is String -> value["checked"] as Boolean
                                        else -> false
                                    },
                                    topic = value["topic"] as? String ?: ""
                                )
                                checklistItems.add(item)
                            }
                        }
                        onResult(checklistItems)
                    } else {
                        println("No such document!")
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error fetching document: $exception")
                }
        }
    }

    fun updateCheckedState(itemId: Int, isChecked: Boolean, userRole: String) {
        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userId)
            .collection("checklists").document(userRole)

        docRef.update(FieldPath.of(itemId.toString(), "checked"), isChecked)
            .addOnSuccessListener { println("Checkbox updated successfully!") }
            .addOnFailureListener { e -> println("Error updating checkbox: $e") }
    }

    fun updateCheckedStateBornLeave(itemId: Int, isChecked: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userId)
            .collection("checklists").document("born_leave_home")

        docRef.update(FieldPath.of(itemId.toString(), "checked"), isChecked)
            .addOnSuccessListener { println("Checkbox updated successfully!") }
            .addOnFailureListener { e -> println("Error updating checkbox: $e") }
    }

    fun updateUserRole(newRole: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .update("role", newRole)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun fetchLeaveHomeChecklist(onResult: (List<ChecklistItem>) -> Unit) {
        val checklistType = "born_leave_home"
        val userId = auth.currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        val docRef = userId?.let {
            db.collection("users").document(it)
                .collection("checklists").document(checklistType)
        }

        if (docRef != null) {
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val checklistItems = mutableListOf<ChecklistItem>()

                        document.data?.forEach { (_, value) ->
                            if (value is Map<*, *>) {
                                val item = ChecklistItem(
                                    id = (value["id"] as? Long)?.toInt() ?: 0,
                                    text = value["text"] as? String ?: "",
                                    checked = when (value["checked"]) {
                                        is Boolean -> value["checked"] as Boolean
                                        is String -> value["checked"] as Boolean
                                        else -> false
                                    },
                                    topic = value["topic"] as? String ?: ""
                                )
                                checklistItems.add(item)
                            }
                        }
                        onResult(checklistItems)
                    } else {
                        println("No such document!")
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error fetching document: $exception")
                }
        }
    }
}
