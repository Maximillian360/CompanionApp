package ph.edu.companionapp.models

data class Owner(
    val id: String,
    val name: String,
    val petCount: Int,
    val ownedPets: List<Pet>
)