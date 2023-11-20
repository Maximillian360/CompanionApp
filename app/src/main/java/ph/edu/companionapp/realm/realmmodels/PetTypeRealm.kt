package ph.edu.companionapp.realm.realmmodels

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class PetTypeRealm : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()
    var petType: String = ""
    var type: Int = 0
}