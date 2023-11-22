package ph.edu.companionapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ph.edu.companionapp.models.Owner

class OwnersViewModel : ViewModel() {

    private val _ownersLiveData = MutableLiveData<List<Owner>>()
    val ownersLiveData: LiveData<List<Owner>> get() = _ownersLiveData

    private val activeOwnersLiveData: MutableLiveData<List<Owner>> = MutableLiveData()
    private val archivedOwnersLiveData: MutableLiveData<List<Owner>> = MutableLiveData()

    // ... (rest of the ViewModel code)

    fun getActiveOwners(): LiveData<List<Owner>> {
        return activeOwnersLiveData
    }

    fun getArchivedOwners(): LiveData<List<Owner>> {
        return archivedOwnersLiveData
    }

    fun updateOwners(owners: List<Owner>) {
        _ownersLiveData.value = owners
    }



}