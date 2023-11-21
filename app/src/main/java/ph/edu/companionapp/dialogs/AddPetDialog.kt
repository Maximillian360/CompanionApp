package ph.edu.companionapp.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.edu.companionapp.R
import ph.edu.companionapp.databinding.DialogAddPetBinding
import ph.edu.companionapp.realm.RealmDatabase

class AddPetDialog : DialogFragment() {

    private lateinit var binding: DialogAddPetBinding
    lateinit var refreshDataCallback: RefreshDataInterface
    private var database = RealmDatabase()


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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogAddPetBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            spType.adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.pet_arrays,
                android.R.layout.simple_spinner_item
            )
            btnAdd.setOnClickListener{
                if(edtPetName.text.isNullOrEmpty()){
                    edtPetName.error = "Required"
                    return@setOnClickListener
                }
                if(edtAge.text.isNullOrEmpty()){
                    edtAge.error = "Required"
                    return@setOnClickListener
                }

                val selectedPetType = spType.selectedItem.toString()
                val petAge = edtAge.text.toString().toInt()
                val ownerName = if (edtOwner.text.isNotEmpty()) edtOwner.text.toString() else "Lotus"

                //TODO: DISCUSSION FOR REALM HERE

                val coroutineContext = Job() + Dispatchers.IO
                val scope = CoroutineScope(coroutineContext + CoroutineName("addPetToRealm"))
                scope.launch(Dispatchers.IO) {
                    database.addPet(edtPetName.text.toString(), petAge, selectedPetType, ownerName)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, "Pet has been added!", Toast.LENGTH_LONG).show()
                        refreshDataCallback.refreshData()
                        dialog?.dismiss()
                    }
                }
            }
        }
    }
}