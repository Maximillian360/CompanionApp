package ph.edu.companionapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ph.edu.companionapp.models.Pet

class PetsViewModel : ViewModel() {

    // Define your LiveData for pets
    private val _petsLiveData = MutableLiveData<List<Pet>>()
    val petsLiveData: LiveData<List<Pet>> get() = _petsLiveData

    // Define functions to update the LiveData
    fun updatePets(pets: List<Pet>) {
        _petsLiveData.value = pets
    }

    // Additional functions as needed

}