package ph.edu.companionapp.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ph.edu.companionapp.adapters.ItemTouchHelperAdapter
import ph.edu.companionapp.models.Owner
import ph.edu.companionapp.adapters.OwnedPetAdapter
import ph.edu.companionapp.databinding.ContentOwnerRvBinding

class OwnerAdapter(
    private var ownerList: ArrayList<Owner>,
    private val context: Context,
    private val ownerAdapterCallback: OwnerAdapterInterface
) : RecyclerView.Adapter<OwnerAdapter.OwnerViewHolder>(),
    ItemTouchHelperAdapter {


    interface OwnerAdapterInterface {
        fun deleteOwnerAndTransferPets(ownerId: String, position: Int)
    }

    inner class OwnerViewHolder(private val binding: ContentOwnerRvBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(itemData: Owner){
            with(binding){
                txtOwnerName.text = String.format("Owner name: %s",itemData.name)
                txtNumPets.text = String.format("Number of pets: %s", itemData.petCount)

                val ownedPetAdapter = OwnedPetAdapter(itemData.ownedPets)
                rvOwnedPets.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                rvOwnedPets.adapter = ownedPetAdapter
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
        val ownerId = ownerList[position].id
        ownerAdapterCallback.deleteOwnerAndTransferPets(ownerId, position)
        ownerList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateList(ownerList: ArrayList<Owner>){
        this.ownerList = arrayListOf()
        notifyDataSetChanged()
        this.ownerList = ownerList
        this.notifyItemInserted(this.ownerList.size)
    }

    fun getOwnerId(position: Int): String? {
        if (position in 0 until ownerList.size) {
            return ownerList[position].id
        }
        return null
    }

}