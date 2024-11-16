import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ys.phdmama.viewmodel.BabyStatusViewModel

class BabyStatusViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BabyStatusViewModel::class.java)) {
            return BabyStatusViewModel(/* any dependencies if needed */) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
