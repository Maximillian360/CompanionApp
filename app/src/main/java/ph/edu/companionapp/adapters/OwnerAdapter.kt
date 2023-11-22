package ph.edu.companionapp.adapters


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ph.edu.companionapp.adapters.ItemTouchHelperAdapter
import ph.edu.companionapp.models.Owner
import ph.edu.companionapp.adapters.OwnedPetAdapter
import ph.edu.companionapp.databinding.ContentOwnerRvBinding
import ph.edu.companionapp.dialogs.EditOwner
import ph.edu.companionapp.dialogs.EditPet

class OwnerAdapter(
    private var ownerList: ArrayList<Owner>,
    private val context: Context,
    private val ownerAdapterCallback: OwnerAdapterInterface,
    private var fragmentManager: FragmentManager
) : RecyclerView.Adapter<OwnerAdapter.OwnerViewHolder>(),
    ItemTouchHelperAdapter {


    interface OwnerAdapterInterface {

        fun archiveOwner(ownerId: String, position: Int)
        fun deleteOwnerAndTransferPets(ownerId: String, position: Int)
        fun refreshData()
    }

    inner class OwnerViewHolder(private val binding: ContentOwnerRvBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(itemData: Owner){
            with(binding){
                txtOwnerName.text = String.format("Owner name: %s",itemData.name)
                txtNumPets.text = String.format("Number of pets: %s", itemData.petCount)

                val ownedPetAdapter = OwnedPetAdapter(itemData.ownedPets)
                rvOwnedPets.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                rvOwnedPets.adapter = ownedPetAdapter

                btnEditOwner.isEnabled = itemData.name != "Lotus"

                btnEditOwner.setOnClickListener {
                    val editOwnerDialog = EditOwner()
                    editOwnerDialog.refreshDataCallback = object : EditPet.RefreshDataInterface{
                        override fun refreshData() {
                            ownerAdapterCallback.refreshData()
                        }
                    }
                    editOwnerDialog.bindOwnerData(itemData)
                    editOwnerDialog.show(fragmentManager, null)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnerViewHolder {
        val binding = ContentOwnerRvBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OwnerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OwnerViewHolder, position: Int) {
        val ownerData = ownerList[position]
        holder.bind(ownerData)
        holder.itemView.tag = position
    }

    override fun getItemCount(): Int {
        return ownerList.size
    }

    override fun onItemDismiss(position: Int) {
        if (position in 0 until ownerList.size) {
            val ownerId = ownerList[position].id
            Log.d("OwnerAdapter", "onItemDismiss - ownerListSize: ${ownerList.size}")
            Log.d("OwnerAdapter", "onItemDismiss - ownerId: $ownerId, position: $position")
            ownerAdapterCallback.archiveOwner(ownerId, position)
        }
        else{
            Log.d("OwnerAdapter", "Error: Position out of bounds")
        }


//        ownerList.removeAt(position)
//        notifyItemRemoved(position)


        //ownerAdapterCallback.deleteOwnerAndTransferPets(ownerId, position)
    }

//    fun updateList(ownerList: ArrayList<Owner>){
//        this.ownerList = arrayListOf()
//        notifyDataSetChanged()
//        this.ownerList = ownerList
//        this.notifyItemInserted(this.ownerList.size)
//    }

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