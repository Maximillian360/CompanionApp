package ph.edu.companionapp.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import ph.edu.companionapp.adapters.PetAdapter
import ph.edu.companionapp.databinding.ActivityPetsBinding
import ph.edu.companionapp.dialogs.AddPetDialog
import ph.edu.companionapp.models.Pet
import ph.edu.companionapp.realm.RealmDatabase
import ph.edu.companionapp.realm.realmmodels.PetRealm

class PetsActivity : AppCompatActivity(),
    AddPetDialog.RefreshDataInterface,
    PetAdapter.PetAdapterInterface {

    private lateinit var binding: ActivityPetsBinding
    private lateinit var petList: ArrayList<Pet>
    private lateinit var adapter: PetAdapter
    private var database = RealmDatabase()
    private lateinit var itemTouchHelper: ItemTouchHelper
    private val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            AlertDialog.Builder(this@PetsActivity)
                .setTitle("Delete")
                .setMessage("Are you sure you want to consign this?")
                .setPositiveButton("Consign") { _, _ ->
                    adapter.onItemDismiss(viewHolder.adapterPosition)

                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // User clicked Cancel, dismiss the dialog
                    adapter.notifyItemChanged(viewHolder.adapterPosition)
                    dialog.dismiss()
                }
                .show()
        }
    }

    interface RefreshDataInterface{
        fun refreshOwners()
        fun refreshData()
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        petList = arrayListOf()
        adapter = PetAdapter(petList, this, this, supportFragmentManager)

        val layoutManager = LinearLayoutManager(this)
        binding.rvPets.layoutManager = layoutManager
        binding.rvPets.adapter = adapter

        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvPets)


        getPets()

        binding.fab.setOnClickListener {
            val addPetDialog = AddPetDialog()
            addPetDialog.refreshDataCallback = this
            addPetDialog.show(supportFragmentManager, null)
        }


        binding.btnSearch.setOnClickListener {
            if (binding.edtSearch.text.toString().isEmpty()) {
                binding.edtSearch.error = "Required"
                return@setOnClickListener
            }

            val coroutineContext = Job() + Dispatchers.IO
            val scope = CoroutineScope(coroutineContext + CoroutineName("SearchPets"))
            scope.launch(Dispatchers.IO) {
                val result = database.getPetsByName(binding.edtSearch.text.toString())
                val petList = arrayListOf<Pet>()
                petList.addAll(
                    result.map {
                        mapPet(it)
                    }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }


    override fun onResume() {
        super.onResume()
        //TODO: REALM DISCUSSION HERE
        getPets()
    }

    override fun refreshData() {
        getPets()


    }

    override fun consignPet(pet: Pet, position: Int) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("consignPet"))

        scope.launch(Dispatchers.IO) {
            try {
                database.consignPet(pet)
                withContext(Dispatchers.Main) {
                    adapter.notifyItemRemoved(position)
                    adapter.updatePetList(database.getOwnedPets().map { mapPet(it) } as ArrayList<Pet>)
                    Snackbar.make(binding.root, "Pet consigned", Snackbar.LENGTH_SHORT).show()
                    getPets()

                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Error deleting pet", Snackbar.LENGTH_SHORT).show()

                }
            }
        }
    }

    override fun deletePet(id: String, position: Int) {

    }




    private fun mapPet(pet: PetRealm): Pet {
        return Pet(
            id = pet.id.toHexString(),
            name = pet.name,
            petType = pet.petType,
            age = pet.age,
            ownerName = pet.owner?.name ?: ""
        )
    }

    private fun getPets() {

        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllPets"))
        scope.launch(Dispatchers.IO) {
            val pets = database.getOwnedPets()
            val petList = arrayListOf<Pet>()
            petList.addAll(
                pets.map {
                    mapPet(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updatePetList(petList)
                adapter.notifyDataSetChanged()
                binding.empty.text = if (petList.isEmpty()) "No Companions Yet..." else ""
            }
        }
    }
}