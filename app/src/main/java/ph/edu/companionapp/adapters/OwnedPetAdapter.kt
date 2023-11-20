package ph.edu.companionapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import ph.edu.companionapp.R
import ph.edu.companionapp.databinding.OwnedPetBinding
import ph.edu.companionapp.models.Pet

class OwnedPetAdapter(private var petList: List<Pet>) :
    RecyclerView.Adapter<OwnedPetAdapter.SimplePetViewHolder>() {

    inner class SimplePetViewHolder(private val binding: OwnedPetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val petTypeImages = mapOf(
            "Sentinel" to R.drawable.sentinel,
            "Kavat" to R.drawable.cat,
            "Kubrow" to R.drawable.dog,
            "Vulpaphyla" to R.drawable.fox,
            "Predasite" to R.drawable.lion,
            "MOA" to R.drawable.chicken,
            "Hound" to R.drawable.hound
        )

        fun bind(itemData: Pet) {
            with(binding) {
                txtPetName.text = String.format("%s", itemData.name)

                val petTypeImage = petTypeImages[itemData.petType]
                if (petTypeImage != null) {
                    imageView.setImageResource(petTypeImage)
                }
                // Set other views for displaying pet information
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimplePetViewHolder {
        val binding =
            OwnedPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SimplePetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SimplePetViewHolder, position: Int) {
        val petData = petList[position]
        holder.bind(petData)
    }

    override fun getItemCount(): Int {
        return petList.size
    }

    fun updatePetList(petList: List<Pet>) {
        this.petList = petList
        notifyDataSetChanged()
    }
}