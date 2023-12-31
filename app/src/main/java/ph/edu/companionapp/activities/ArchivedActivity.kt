package ph.edu.companionapp.activities


import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
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
import ph.edu.companionapp.adapters.ArchivedAdapter
import ph.edu.companionapp.models.Pet
import ph.edu.companionapp.realm.realmmodels.OwnerRealm
import ph.edu.companionapp.databinding.ActivityArchivedBinding
import ph.edu.companionapp.models.Owner
import ph.edu.companionapp.realm.RealmDatabase
import ph.edu.companionapp.realm.realmmodels.PetRealm
import ph.edu.companionapp.viewmodels.OwnersViewModel


class ArchivedActivity : AppCompatActivity(),
    //PetAdapter.PetAdapterInterface,
    ArchivedAdapter.ArchiveAdapterInterface {
    private lateinit var binding : ActivityArchivedBinding
    private lateinit var adapter: ArchivedAdapter
    private lateinit var ownerList: ArrayList<Owner>
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var database = RealmDatabase()

    private val swipeToDeleteCallback  = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val ownerId = adapter.getOwnerId(position)


            // Handle swipe for Lotus owner
            if (ownerId == "Lotus") {
                // Notify adapter to refresh the view
                adapter.notifyItemChanged(position)
            } else {
                AlertDialog.Builder(this@ArchivedActivity)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete this?")
                    .setPositiveButton("Delete") { _, _ -> adapter.onItemDismiss(position)

                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        // User clicked Cancel, dismiss the dialog
                        adapter.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .show()
                // Handle swipe for other owners

            }
        }
    }

    override fun refreshData() {
        // Implement data refresh logic in OwnersActivity or wherever necessary
        getOwners() // or any other logic to refresh data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchivedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ownerList = arrayListOf()
        adapter = ArchivedAdapter(ownerList,this, this, supportFragmentManager)
        binding.rvOwner.adapter = adapter

        getOwners()

        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvOwner)

        val layoutManger = LinearLayoutManager(this)
        binding.rvOwner.layoutManager = layoutManger

    }

    override fun onResume() {
        super.onResume()
        getOwners()
    }

    override fun onPause() {
        super.onPause()
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

    //TODO: REALM DISCUSSION HERE
    private fun mapOwner(owner: OwnerRealm): Owner {
        return Owner(
            id = owner.id.toHexString(),
            name = owner.name,
            petCount = owner.pets.size,
            ownedPets = owner.pets.map {mapPet(it)}
        )
    }



    fun getOwners() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllOwners"))
        scope.launch(Dispatchers.IO) {
            val owners = database.getArchivedOwners()
            val ownerList = arrayListOf<Owner>()

            ownerList.addAll(
                owners.map {
                    mapOwner(it)
                }
            )
            withContext(Dispatchers.Main) {
                binding.empty.text = if (ownerList.isEmpty()) "No Archived Tenno Yet..." else ""
                adapter.updateList(ownerList)
                adapter.notifyDataSetChanged()

            }
        }
    }



    override fun deleteOwnerAndTransferPets(ownerId: String, position: Int) {
        // Implement the logic to transfer pets to Lotus and delete the owner
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("deleteOwnerAndTransferPets"))

        scope.launch(Dispatchers.IO) {
            try {
                database.deleteOwnerAndTransferPets(BsonObjectId(ownerId))
                withContext(Dispatchers.Main) {
                    adapter.notifyItemRemoved(position)
                    adapter.updateList(database.getArchivedOwners().map { mapOwner(it) } as ArrayList<Owner>)
                    Snackbar.make(binding.root, "Owner deleted and pets transferred to Lotus", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle deletion error
                withContext(Dispatchers.Main) {
                    adapter.notifyItemChanged(position)
                    Snackbar.make(binding.root, "Error deleting owner", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}