package ph.edu.companionapp.activities

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import ph.edu.companionapp.adapters.ConsignedAdapter
import ph.edu.companionapp.adapters.PetAdapter
import ph.edu.companionapp.databinding.ActivityConsignedBinding
import ph.edu.companionapp.models.Pet
import ph.edu.companionapp.realm.RealmDatabase
import ph.edu.companionapp.realm.realmmodels.PetRealm

class ConsignedActivity : AppCompatActivity(),
    ConsignedAdapter.ConsignedAdapterInterface {

    private lateinit var binding: ActivityConsignedBinding
    private lateinit var petList: ArrayList<Pet>
    private lateinit var adapter: ConsignedAdapter
    private var database = RealmDatabase()
    private lateinit var itemTouchHelper: ItemTouchHelper
    private val swipeToDeleteCallback  = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            AlertDialog.Builder(this@ConsignedActivity)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this?")
                .setPositiveButton("Delete") { _, _ -> adapter.onItemDismiss(viewHolder.adapterPosition)

                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // User clicked Cancel, dismiss the dialog
                    adapter.notifyItemChanged(viewHolder.adapterPosition)
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsignedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        petList = arrayListOf()
        adapter = ConsignedAdapter(petList,this,this, supportFragmentManager)

        val layoutManager = LinearLayoutManager(this)
        binding.rvPets.layoutManager = layoutManager
        binding.rvPets.adapter = adapter

        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvPets)


        binding.btnSearch.setOnClickListener{
            if(binding.edtSearch.text.toString().isEmpty()){
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



    override fun onResume() {
        super.onResume()
        //TODO: REALM DISCUSSION HERE
        getPets()
    }

    override fun onPause() {
        super.onPause()
        getPets()
    }

    override fun refreshData() {
        //TODO: REALM DISCUSSION HERE
        getPets()
    }



    private fun mapPet(pet: PetRealm) : Pet {
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
            val pets = database.getConsignedPets()
            val petList = arrayListOf<Pet>()
            petList.addAll(
                pets.map {
                    mapPet(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updatePetList(petList)
                adapter.notifyDataSetChanged()
            }
        }
    }
        override fun deletePet(id: String, position: Int) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("deletePet"))


        scope.launch(Dispatchers.IO) {
            try {
                database.deletePet(BsonObjectId(id))
                withContext(Dispatchers.Main) {
                    // Notify the adapter after successful deletion
                    adapter.notifyItemRemoved(position)
                    adapter.updatePetList(database.getConsignedPets().map { mapPet(it) } as ArrayList<Pet>)
                    Snackbar.make(binding.root, "Pet deleted", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle deletion error
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Error deleting pet", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }


}