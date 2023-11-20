package ph.edu.companionapp.realm.realmmodels

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class PetRealm : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()
    var name: String = ""
    var age: Int = 0
    var petType: String = ""
    var owner: OwnerRealm? = null
}