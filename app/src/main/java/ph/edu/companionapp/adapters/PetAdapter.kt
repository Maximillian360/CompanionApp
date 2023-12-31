package ph.edu.companionapp.adapters


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import ph.edu.companionapp.R
import ph.edu.companionapp.databinding.ContentPetRvBinding
import ph.edu.companionapp.dialogs.AdoptPetDialog
import ph.edu.companionapp.dialogs.EditPet
import ph.edu.companionapp.models.Pet

class PetAdapter(
    private var petList: ArrayList<Pet>,
    private var context: Context,
    private var petAdapterCallback: PetAdapterInterface,
    private var fragmentManager: FragmentManager
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>(),
    ItemTouchHelperAdapter {

    val petTypeImages = mapOf(
        "Sentinel" to R.drawable.sentinel,
        "Kavat" to R.drawable.cat,
        "Kubrow" to R.drawable.dog,
        "Vulpaphyla" to R.drawable.fox,
        "Predasite" to R.drawable.lion,
        "MOA" to R.drawable.chicken,
        "Hound" to R.drawable.hound
    )

    interface PetAdapterInterface {
        fun deletePet(id: String, position: Int)
        fun refreshData()
        fun consignPet(pet: Pet, position: Int)

    }

    inner class PetViewHolder(private val binding: ContentPetRvBinding) :
        RecyclerView.ViewHolder(binding.root), ItemTouchHelperViewHolder {
        override fun onItemSelected() {
            // No additional action on item selection
        }

        override fun onItemClear() {
            // No additional action on item clear
        }
        fun bind(itemData: Pet) {
            with(binding) {
                txtAge.text = String.format("Age: %s", itemData.age.toString())
                txtPetName.text = String.format("Pet name: %s", itemData.name)
                txtPetType.text = String.format("Pet type: %s", itemData.petType)
                if (itemData.ownerName.isNotEmpty()) {
                    txtOwnerName.visibility = View.VISIBLE
                    txtOwnerName.text = String.format("Owner name: %s", itemData.ownerName)
                } else {
                    txtOwnerName.visibility = View.GONE
                }

                val petTypeImage = petTypeImages[itemData.petType]
                if (petTypeImage != null) {
                    imageView.setImageResource(petTypeImage)
                }


                btnAdopt.isEnabled = itemData.ownerName == "Lotus"

                btnAdopt.setOnClickListener {
                    val adoptPetDialog = AdoptPetDialog()
                    adoptPetDialog.refreshDataCallback = object : AdoptPetDialog.RefreshDataInterface {
                        override fun refreshData() {
                            // Implement the logic to refresh the data
                            // This might involve updating the RecyclerView, for example
                            petAdapterCallback.refreshData()
                        }
                    }
                    adoptPetDialog.setPet(itemData)
                    adoptPetDialog.show(fragmentManager, null)

                }

                btnEditPet.setOnClickListener {
                    val editPet = EditPet()
                    editPet.refreshDataCallback = object : EditPet.RefreshDataInterface{
                        override fun refreshData() {
                            petAdapterCallback.refreshData()
                        }

                    }
                    editPet.bindPetData(itemData)
                    editPet.show(fragmentManager, null)
                    Log.d("PetAdapter", "Opening EditPet dialog for pet: $itemData")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ContentPetRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val petData = petList[position]
        holder.bind(petData)
    }

    override fun getItemCount(): Int {
        return petList.size
    }
    override fun onItemDismiss(position: Int) {
        // Remove the swiped pet from the list
        if (position in 0 until petList.size) {
            val petId = petList[position].id
            petAdapterCallback.consignPet(petList[position], position)
            //petAdapterCallback.deletePet(petId, position)
            petList.removeAt(position)
            notifyItemRemoved(position)
        }
        else{
            Log.d("PetAdapter", "Error")
        }

    }

    fun updatePetList(petList: ArrayList<Pet>) {
        this.petList.clear()
        this.petList.addAll(petList)
        notifyDataSetChanged()
    }

}
