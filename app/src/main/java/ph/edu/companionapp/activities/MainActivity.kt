package ph.edu.companionapp.activities


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ph.edu.companionapp.R
import ph.edu.companionapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOwnerList.setOnClickListener(this)
        binding.btnPetList.setOnClickListener(this)
        binding.btnArchived.setOnClickListener(this)
        binding.btnConsigned.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.btn_owner_list -> {
                val intent = Intent(this, OwnersActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_pet_list -> {
                val intent = Intent(this, PetsActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_archived -> {
                val intent = Intent(this, ArchivedActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_consigned -> {
                val intent = Intent(this, ConsignedActivity::class.java)
                startActivity(intent)
            }
        }
    }
}