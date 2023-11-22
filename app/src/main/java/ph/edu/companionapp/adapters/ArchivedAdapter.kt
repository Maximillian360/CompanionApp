package ph.edu.companionapp.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
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
import ph.edu.companionapp.databinding.ActivityArchivedBinding
import ph.edu.companionapp.databinding.ContentArchivedRvBinding
import ph.edu.companionapp.databinding.ContentOwnerRvBinding
import ph.edu.companionapp.models.Owner
import ph.edu.companionapp.models.Pet
import ph.edu.companionapp.realm.RealmDatabase
import ph.edu.companionapp.realm.realmmodels.OwnerRealm
import ph.edu.companionapp.realm.realmmodels.PetRealm

class ArchivedAdapter(
    private var ownerList: ArrayList<Owner>,
    private val context: Context,
    private val archiveAdapterCallback: ArchiveAdapterInterface,
    private var fragmentManager: FragmentManager
) : RecyclerView.Adapter<ArchivedAdapter.ArchiveViewHolder>(), ItemTouchHelperAdapter {
    private var database = RealmDatabase()

    interface ArchiveAdapterInterface {
        fun deleteOwnerAndTransferPets(ownerId: String, position: Int)
        fun refreshData()
    }

    inner class ArchiveViewHolder(private val binding: ContentArchivedRvBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemData: Owner) {
            with(binding) {
                txtOwnerName.text = String.format("Owner name: %s", itemData.name)
                txtNumPets.text = String.format("Number of pets: %s", itemData.petCount)

                val ownedPetAdapter = OwnedPetAdapter(itemData.ownedPets)
                rvOwnedPets.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                rvOwnedPets.adapter = ownedPetAdapter

                btnRestore.setOnClickListener {

                    val coroutineContext = Job() + Dispatchers.IO
                    val scope =
                        CoroutineScope(coroutineContext + CoroutineName("restoreOwnerToRealm"))
                    scope.launch(Dispatchers.IO) {
                        try {
                            database.restoreOwner(itemData)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Owner has been restored!", Toast.LENGTH_LONG).show()
                                archiveAdapterCallback.refreshData()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveViewHolder {
        val binding =
            ContentArchivedRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArchiveViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArchivedAdapter.ArchiveViewHolder, position: Int) {
        val ownerData = ownerList[position]
        holder.bind(ownerData)

    }

    override fun getItemCount(): Int {
        return ownerList.size
    }

    override fun onItemDismiss(position: Int) {
        val ownerId = ownerList[position].id
        archiveAdapterCallback.deleteOwnerAndTransferPets(ownerId, position)
        ownerList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateList(ownerList: ArrayList<Owner>) {
        this.ownerList.clear()
        this.ownerList.addAll(ownerList)
        notifyDataSetChanged()
    }

    fun getOwnerId(position: Int): String? {
        if (position in 0 until ownerList.size) {
            return ownerList[position].id
        }
        return null
    }
}
