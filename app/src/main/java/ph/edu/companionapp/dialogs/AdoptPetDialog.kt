package ph.edu.companionapp.dialogs


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.edu.companionapp.databinding.DialogAdoptPetBinding
import ph.edu.companionapp.models.Pet
import ph.edu.companionapp.realm.RealmDatabase

class AdoptPetDialog : DialogFragment() {

    private lateinit var binding: DialogAdoptPetBinding
    lateinit var refreshDataCallback: RefreshDataInterface
    private lateinit var pet: Pet
    private var database = RealmDatabase()

    interface RefreshDataInterface {
        fun refreshData()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun setPet(pet: Pet) {
        Log.d("AdoptPetDialog", "Setting pet: $pet")
        this.pet = pet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAdoptPetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            btnAdopt.setOnClickListener {
                if (edtOwner.text.isNullOrEmpty()) {
                    edtOwner.error = "Required"
                    return@setOnClickListener
                }

                val ownerName = edtOwner.text.toString()
                val coroutineContext = Job() + Dispatchers.IO
                val scope = CoroutineScope(coroutineContext + CoroutineName("adoptPet"))
                scope.launch(Dispatchers.IO) {
                    try {
                        // Call the adoptPet function from your RealmDatabase
                        database.adoptPet(pet, ownerName)
                        withContext(Dispatchers.Main) {
                            // You can update the UI or show a message if needed
                            Toast.makeText(activity, "Pet adopted!", Toast.LENGTH_LONG).show()
                            // Refresh data if necessary
                            refreshDataCallback.refreshData()
                            dialog?.dismiss()
                        }
                    }
                    catch (e: Exception) {
                        // Log the exception for debugging
                        Log.e("AdoptPet", "Error adopting Pet", e)
                        // Handle adoption error
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, "Error ADOPTING pet", Toast.LENGTH_LONG).show()

                        }
                    }
                }
            }
        }
    }
}