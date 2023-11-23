package ph.edu.companionapp.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import ph.edu.companionapp.databinding.EditOwnerBinding
import ph.edu.companionapp.models.Owner
import ph.edu.companionapp.models.Pet
import ph.edu.companionapp.realm.RealmDatabase

class EditOwner : DialogFragment() {
    private lateinit var binding: EditOwnerBinding
    lateinit var refreshDataCallback: EditPet.RefreshDataInterface
    private var database = RealmDatabase()
    private lateinit var owner: Owner

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

    fun bindOwnerData(owner: Owner) {
        Log.d("EditPet", "Updating pet: $owner")
        this.owner = owner

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EditOwnerBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            edtOwnerUpd.setText(owner.name)
            btnEditOwner.setOnClickListener {
                if (edtOwnerUpd.text.isNullOrEmpty()) {
                    edtOwnerUpd.error = "Required"
                    return@setOnClickListener
                }
                val name = edtOwnerUpd.text.toString()
                val id = owner.id
                val coroutineContext = Job() + Dispatchers.IO
                val scope = CoroutineScope(coroutineContext + CoroutineName("editOwnerToRealm"))
                Log.d("EditOwner", "Updating pet: id=$id, name=$name")
                scope.launch(Dispatchers.IO) {
                    try {
                        if (database.ownerNameTaken(name)) {
                            withContext(Dispatchers.Main) {
                                edtOwnerUpd.error = ""
                                Snackbar.make(
                                    binding.root,
                                    "Owner name is taken",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            database.updateOwner(BsonObjectId(id), name)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    activity,
                                    "Owner has been updated!",
                                    Toast.LENGTH_LONG
                                ).show()
                                refreshDataCallback.refreshData()
                                dialog?.dismiss()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("EditOwner", "Error updating OWNER", e)
                        withContext(Dispatchers.Main) {
                            Snackbar.make(
                                binding.root,
                                "Error updating owner",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}