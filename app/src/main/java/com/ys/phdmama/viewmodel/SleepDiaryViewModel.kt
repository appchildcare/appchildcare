import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

//@HiltViewModel
class SleepDiaryViewModel /* @Inject constructor() */: ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _napEntries = MutableStateFlow<List<DayNapEntry>>(emptyList())
    val napEntries: StateFlow<List<DayNapEntry>> = _napEntries

    fun fetchNapData(babyId: String?) {
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return

        if (babyId != null) {
            db.collection("users")
                .document(userId)
                .collection("babies")
                .document(babyId)
                .collection("nap_counter_time")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val groupedByDay = result.documents.groupBy {
                        val ts = it.getTimestamp("timestamp")?.toDate()
                        SimpleDateFormat("EEEE dd, yyyy", Locale("es")).format(ts ?: Date())
                    }

                    val dayEntries = groupedByDay.map { (day, docs) ->
                        DayNapEntry(
                            dayName = day,
                            naps = docs.map { doc ->
                                val ts = doc.getTimestamp("timestamp")?.toDate()
                                val timeParts = doc.getString("time")?.split(":") ?: listOf("0", "0")
                                val durationSec = timeParts[0].toInt() * 60 + timeParts[1].toInt()
                                val startHour = ts?.let {
                                    val cal = Calendar.getInstance().apply { time = it }
                                    cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
                                } ?: 0f
                                Nap(
                                    startHourFraction = startHour,
                                    durationHours = durationSec / 60f
                                )
                            }
                        )
                    }
                    _napEntries.value = dayEntries
                }
        }
    }
}
