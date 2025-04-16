import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ys.phdmama.viewmodel.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class PregnancyTrackerViewModel(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _pregnancyTrackingData = MutableStateFlow<User?>(null)
    val currentPregnancyTracking: StateFlow<User?> get() = _pregnancyTrackingData

    fun savePregnancyTracker(
        birthProximateDate: Date,
        ecoWeeks: Int,
        lastMenstruationDate: Date
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = firebaseAuth.currentUser
            currentUser?.let {
                val pregnancyTracker = hashMapOf(
                    "birthProximateDate" to birthProximateDate,
                    "ecoWeeks" to ecoWeeks,
                    "lastMenstruationDate" to lastMenstruationDate,
                    "userUid" to currentUser.uid
                )

                firestore.collection("pregnancy_tracking")
                    .document(it.uid)
                    .set(pregnancyTracker, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("SUCCESS SAVED", "pregnancyTracker saved $pregnancyTracker")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ERROR PREGNANCY", "detail $e")
                    }
            }
        }
    }

    fun fetchPregnancyTracking() {
        viewModelScope.launch {
            // Get the current user's UID
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.e("FirestoreError", "No user is currently signed in")
                return@launch
            }
            firestore.collection("pregnancy_tracking")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d("FirestoreSuccess", "Fetched current pregnancy data")
                        val user = document.toObject(User::class.java)
                        _pregnancyTrackingData.value = user
                    } else {
                        Log.e("FirestoreError", "No user document found for UID: $currentUserId")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreError", "Error fetching users", exception)
                }
        }
    }
}
