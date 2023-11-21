package ph.edu.companionapp.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import ph.edu.companionapp.R
import ph.edu.companionapp.databinding.DialogAddPetBinding
import ph.edu.companionapp.databinding.EditPetBinding
import ph.edu.companionapp.models.Pet
import ph.edu.companionapp.realm.RealmDatabase

class EditPet : DialogFragment() {
    private lateinit var binding: EditPetBinding
    lateinit var refreshDataCallback: RefreshDataInterface
    private var database = RealmDatabase()
    private lateinit var pet: Pet
    interface RefreshDataInterface{
        fun refreshData()
    }



    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun bindPetData(pet: Pet) {
        Log.d("EditPet", "Updating pet: $pet")
        this.pet = pet

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EditPetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            spType.adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.pet_arrays,
                android.R.layout.simple_spinner_item
            )

            binding.edtPetName.setText(pet.name)
            binding.edtAge.setText(pet.age.toString())
            edtOwner.setText(pet.ownerName)
            //binding.edtOwner.setText("Lotus")
            val petTypes = resources.getStringArray(R.array.pet_arrays)
            val typeIndex = petTypes.indexOf(pet.petType)
            binding.spType.setSelection(typeIndex)

            btnEditPet.setOnClickListener{
                if (edtPetName.text.isNullOrEmpty()) {
                    edtPetName.error = "Required"
                    return@setOnClickListener
                }
                if (edtAge.text.isNullOrEmpty()) {
                    edtAge.error = "Required"
                    return@setOnClickListener
                }


                val petName = edtPetName.text.toString()
                val selectedPetType = spType.selectedItem.toString()
                val petAge = edtAge.text.toString().toInt()

                //val ownerName = if (edtOwner.text.isNotEmpty()) edtOwner.text.toString() else "Lotus"
                val petId = pet.id

                Log.d("EditPet", "Updating pet: id=$petId, name=$petName, age=$petAge, type=$selectedPetType")

                val coroutineContext = Job() + Dispatchers.IO
                val scope = CoroutineScope(coroutineContext + CoroutineName("editPetToRealm"))
                scope.launch(Dispatchers.IO) {
                    try{
                        database.updatePet(BsonObjectId(petId),petName, petAge, selectedPetType)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, "Pet has been updated!", Toast.LENGTH_LONG).show()
                            refreshDataCallback.refreshData()
                            dialog?.dismiss()
                        }
                    }
                    catch(e: Exception){
                        Log.e("EditPet", "Error updating pet", e)
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, "Error updating pet", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}