package com.ys.phdmama.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ys.phdmama.ui.components.PhdLayoutMenu

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
            text = "Términos y Condiciones de Uso de ChildCare app \n",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Fecha de última actualización: 3 de septiembre de 2025 \n",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Fecha de última actualización: 3 de septiembre de 2025\n\n" +
                    "Bienvenido/a a ChildCare app. Antes de utilizar nuestra aplicación, te pedimos que leas detenidamente " +
                    "los siguientes Términos y Condiciones de Uso (\"Términos\"). Al descargar, acceder o utilizar la " +
                    "aplicación móvil ChildCare app (en adelante, la \"Aplicación\"), confirmas que has leído, entendido " +
                    "y aceptado vincularte legalmente por estos Términos. Si no estás de acuerdo con ellos, por favor, " +
                    "no utilices la Aplicación.\n\n" +

                    "1. Aceptación de los Términos\n\n" +
                    "El uso de los servicios de ChildCare app implica la aceptación plena y sin reservas de todas y cada " +
                    "una de las disposiciones incluidas en estos Términos. Nos reservamos el derecho de modificar estos " +
                    "Términos en cualquier momento. Notificaremos los cambios materiales a través de la Aplicación o por " +
                    "otros medios para que puedas revisarlos antes de continuar utilizando nuestros servicios. El uso " +
                    "continuado de la Aplicación después de dichas modificaciones constituirá tu aceptación de los nuevos " +
                    "Términos.\n\n" +

                    "2. Descripción del Servicio\n\n" +
                    "ChildCare app es una aplicación móvil diseñada para ofrecer apoyo y orientación a padres y madres " +
                    "primerizos durante las etapas de gestación, parto y los primeros años de crianza. Nuestras " +
                    "funcionalidades incluyen:\n" +
                    "• Herramientas para el seguimiento del embarazo y el desarrollo del bebé.\n" +
                    "• Una agenda y calendario para registrar y recordar citas médicas, vacunas y otros hitos importantes.\n" +
                    "• La capacidad de generar reportes informativos basados en los datos que voluntariamente introduces.\n" +
                    "• Una biblioteca de contenidos con artículos, guías y consejos sobre temas relacionados con la maternidad, paternidad y crianza.\n\n" +

                    "3. Importante: Descargo de Responsabilidad Médica\n\n" +
                    "Esta sección es de vital importancia. Tu seguridad y la de tu hijo/a son nuestra máxima prioridad, y " +
                    "es fundamental que comprendas el propósito y las limitaciones de nuestra Aplicación.\n\n" +
                    "• La Aplicación NO es un Sustituto del Consejo Médico Profesional: ChildCare app es una herramienta de apoyo " +
                    "y seguimiento meramente informativo. Bajo NINGUNA circunstancia debe ser utilizada como un sustituto del " +
                    "diagnóstico, la consulta, el criterio, el consejo o el tratamiento proporcionado por un pediatra, " +
                    "ginecólogo, matrona, o cualquier otro profesional de la salud debidamente cualificado.\n\n" +
                    "• Naturaleza Meramente Orientativa del Contenido: Toda la información presentada en la Aplicación y en su " +
                    "página web asociada —incluyendo artículos, consejos, recordatorios, notificaciones y reportes generados " +
                    "por la agenda— tiene un propósito exclusivamente educativo y orientativo.\n\n" +
                    "• Advertencia para Usuarios con Condiciones Médicas Específicas: Si tú (la madre gestante) o tu bebé " +
                    "habéis sido diagnosticados con patologías diversas, condiciones preexistentes, alergias, intolerancias " +
                    "o cualquier otra situación de salud particular, es imperativo que sigas meticulosa y exclusivamente las " +
                    "indicaciones y el plan de tratamiento pautado por tu equipo médico.\n\n" +
                    "• Exención Total de Responsabilidad sobre las Decisiones del Usuario: Al aceptar estos Términos, comprendes " +
                    "y aceptas que tú eres el único/a responsable de todas las decisiones relacionadas con tu salud, tu " +
                    "bienestar y el de tu hijo/a.\n\n" +

                    "4. Responsabilidades del Usuario\n\n" +
                    "Como usuario de ChildCare app, te comprometes a:\n" +
                    "• Proporcionar información veraz y precisa en tu perfil y registros.\n" +
                    "• Utilizar la Aplicación de manera responsable y para los fines para los que fue diseñada.\n" +
                    "• Consultar siempre a un profesional de la salud cualificado ante cualquier duda o preocupación sobre tu salud o la de tu hijo/a.\n" +
                    "• Mantener la confidencialidad de tu cuenta y contraseña.\n\n" +

                    "5. Propiedad Intelectual\n\n" +
                    "Todos los contenidos de la Aplicación y de su sitio web asociado www.appchildcare.com son propiedad de " +
                    "ChildCare app o de sus licenciantes y están protegidos por las leyes y tratados internacionales de " +
                    "propiedad intelectual.\n\n" +

                    "6. Limitación de Responsabilidad\n\n" +
                    "Además del descargo de responsabilidad médica detallado en la sección 3, ChildCare app no garantiza que " +
                    "la Aplicación esté libre de errores o interrupciones.\n\n" +

                    "7. Legislación Aplicable, Jurisdicción y Resolución de Conflictos\n\n" +
                    "Para ofrecer nuestros servicios a nivel global, hemos adoptado un marco legal que busca ser justo y " +
                    "predecible tanto para ti como para nosotros.\n\n" +
                    "Legislación Aplicable: Estos Términos y tu relación con ChildCare app se regirán e interpretarán de acuerdo " +
                    "con las leyes de España, sin tener en cuenta sus disposiciones sobre conflicto de leyes.\n\n" +
                    "Resolución Amistosa de Conflictos: Antes de iniciar cualquier acción legal formal, ambas partes acuerdan " +
                    "intentar resolver cualquier disputa, reclamación o controversia de manera informal. Puedes notificarnos " +
                    "iniciando el proceso a través de childcareapp XXXXXXXX correo electrónico de contacto para soporte legal. " +
                    "Haremos lo posible por resolverlo mediante consulta y negociación de buena fe.\n\n" +
                    "Jurisdicción Competente: Cualquier disputa legal que no pueda resolverse de manera amistosa se someterá a " +
                    "la jurisdicción exclusiva de los tribunales ubicados en Pamplona, España.\n\n" +
                    "Disposiciones para Usuarios en la Unión Europea: Si eres un consumidor residente en la Unión Europea, esta " +
                    "cláusula no te priva de la protección que te ofrecen las disposiciones imperativas de la legislación del " +
                    "país de la UE en el que resides. Tendrás derecho a presentar cualquier reclamación relacionada con estos " +
                    "Términos ante un tribunal competente de tu país de residencia.\n\n",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify,
        )
    }
}
