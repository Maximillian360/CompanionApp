package ph.edu.companionapp.realm

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import ph.edu.companionapp.models.Pet
import ph.edu.companionapp.realm.realmmodels.OwnerRealm
import ph.edu.companionapp.realm.realmmodels.PetRealm
import ph.edu.companionapp.realm.realmmodels.PetTypeRealm
import java.lang.IllegalStateException
import java.nio.file.Files.delete

class RealmDatabase {
    private val realm: Realm by lazy {
        val config = RealmConfiguration
            .Builder(schema = setOf(PetRealm::class, OwnerRealm::class, PetTypeRealm::class))
            .schemaVersion(31)
            .initialData {
                val lotusOwner = OwnerRealm().apply {
                    this.name = "Lotus"
                }
                copyToRealm(lotusOwner)
                copyToRealm(PetTypeRealm().apply {
                    petType = "Sentinel"
                    type = 1
                })
                copyToRealm(PetTypeRealm().apply {
                    petType = "Kavat"
                    type = 2
                })
                copyToRealm(PetTypeRealm().apply {
                    petType = "Kubrow"
                    type = 3
                })
                copyToRealm(PetTypeRealm().apply {
                    petType = "Vulpaphyla"
                    type = 4
                })
                copyToRealm(PetTypeRealm().apply {
                    petType = "Predasite"
                    type = 5
                })
                copyToRealm(PetTypeRealm().apply {
                    petType = "MOA"
                    type = 6
                })
                copyToRealm(PetTypeRealm().apply {
                    petType = "Hound"
                    type = 7
                })
            }
            .build()
        Realm.open(config)
    }

    // Get all pets as normal
    fun getAllPets(): List<PetRealm> {
        return realm.query<PetRealm>().find()
    }

    // Get all pets as Flows (Compose)
    fun getAllPetsAsFlow(): Flow<List<PetRealm>> {
        return realm.query<PetRealm>().asFlow().map { it.list }
    }

    // Search Query
    fun getPetsByName(name: String): List<PetRealm> {
        return realm.query<PetRealm>("name CONTAINS $0", name).find()
    }

    suspend fun addPet(name: String, age: Int, type: String, ownerName: String = "Lotus") {
        realm.write {
            val pet = PetRealm().apply {
                this.name = name
                this.age = age
                this.petType = type
            }

            val managePet = copyToRealm(pet)

            if (ownerName.isNotEmpty() && ownerName != "Lotus") {
                // Check if there's an owner
                val ownerResult: OwnerRealm? =
                    realm.query<OwnerRealm>("name == $0", ownerName).first().find()

                if (ownerResult == null) {
                    // If there's no owner
                    val owner = OwnerRealm().apply {
                        this.name = ownerName
                        this.pets.add(managePet)
                    }

                    val manageOwner = copyToRealm(owner)
                    managePet.owner = manageOwner
                } else {
                    // If there's an owner
                    findLatest(ownerResult)?.pets?.add(managePet)
                    findLatest(managePet)?.owner = findLatest(ownerResult)
                }
            } else {
                val lotusOwner: OwnerRealm? =
                    realm.query<OwnerRealm>("name == $0", "Lotus").first().find()

                if (lotusOwner == null) {
                    // If there's no owner
                    val owner = OwnerRealm().apply {
                        this.name = ownerName
                        this.pets.add(managePet)
                    }

                    val manageOwner = copyToRealm(owner)
                    managePet.owner = manageOwner
                } else {
                    // If there's an owner
                    findLatest(lotusOwner)?.pets?.add(managePet)
                    findLatest(managePet)?.owner = findLatest(lotusOwner)
                }
            }
        }
    }


    suspend fun adoptPet(pet: Pet, ownerName: String) {
        realm.write {
            try {
                val petId = BsonObjectId(pet.id)

                // Query the PetRealm using the converted string ID
                val petRealm = query<PetRealm>("id == $0", petId).first().find()

                val newOwnerResult: OwnerRealm? = realm.query<OwnerRealm>("name == $0", ownerName).first().find()

                if (newOwnerResult == null) {
                    // If there's no owner with the specified name
                    val newOwner = OwnerRealm().apply {
                        this.name = ownerName
                        this.pets.add(petRealm!!)
                    }
                    val managedNewOwner = copyToRealm(newOwner)
                    petRealm?.owner?.pets?.remove(petRealm)
                    petRealm?.owner = managedNewOwner
                } else {

                    // If there's an owner with the specified name
                    val managedNewOwner = findLatest(newOwnerResult)
                    petRealm?.owner?.pets?.remove(petRealm)
                    petRealm?.owner = managedNewOwner

                    // Make sure to add the pet to the new owner's list
                    managedNewOwner?.pets?.add(petRealm!!)
                }
            } catch (e: Exception) {

                // Handle exception if there's an issue with the adoption
                throw IllegalStateException("Error Adopting Pet", e)
            }
        }
    }

        suspend fun updatePet(id: ObjectId, name: String, age: Int, type: String) {
        realm.write {
            val petRealm = realm.query<PetRealm>("id == $0", id).first().find()
            val lotusOwner: OwnerRealm? = realm.query<OwnerRealm>("name == $0", "Lotus").first().find()
            try {
                // Query the PetRealm using the converted string ID

                if (petRealm != null) {
                    // If the pet with the specified ID exists, update its properties
                    findLatest(petRealm).apply {
                        this!!.name = name
                        this.age = age
                        this.petType = type

                    }
                } else {
                    // Handle the case where the pet with the specified ID doesn't exist
                    throw IllegalStateException("Pet with ID $id not found. Cannot update.")
                }
            } catch (e: Exception) {
                // Handle exception if there's an issue with the update
                throw IllegalStateException("Error updating Pet", e)
            }
        }
    }


    suspend fun updateOwner(id: ObjectId, name: String) {
        val existingOwner = realm.query<OwnerRealm>("id == $0", id).first().find()
        realm.write {
            if (existingOwner != null) {
                findLatest(existingOwner).apply {
                    this!!.name = name
                }
            } else {
                throw IllegalStateException("Owner with ID $id not found. Cannot update.")
            }
        }
    }


    // Delete Pet
    suspend fun deletePet(id: ObjectId) {
        realm.write {
            query<PetRealm>("id == $0", id)
                .first()
                .find()
                ?.let { delete(it) }
                ?: throw IllegalStateException("Pet not found")
        }
    }


    suspend fun deleteOwnerAndTransferPets(ownerId: ObjectId) {
        realm.write {
            try {
                // Check if the owner is Lotus
                val ownerRealm = query<OwnerRealm>("id == $0", ownerId).first().find()
                if (ownerRealm?.name == "Lotus") {
                    throw IllegalStateException("Cannot delete Lotus owner")
                }

                // Transfer pets to Lotus
                val lotusOwner: OwnerRealm? =
                    query<OwnerRealm>("name == $0", "Lotus").first().find()
                ownerRealm?.pets?.forEach { petRealm ->
                    lotusOwner?.pets?.add(petRealm)
                    petRealm.owner = lotusOwner
                }

                // Delete the owner
                ownerRealm?.let { delete(it) }
            } catch (e: Exception) {
                // Log the exception for debugging
                Log.e("DeleteOwner", "Error deleting Owner", e)
                // Handle exception if there's an issue with the deletion
                throw IllegalStateException("Error Deleting Owner", e)
            }
        }
    }

    fun getAllOwners(): List<OwnerRealm> {
        return realm.query<OwnerRealm>().find()
    }
}