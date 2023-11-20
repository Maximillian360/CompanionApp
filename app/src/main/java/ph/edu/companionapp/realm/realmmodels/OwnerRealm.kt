package ph.edu.companionapp.realm.realmmodels


import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class OwnerRealm : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()
    var name: String = ""
    var pets: RealmList<PetRealm> = realmListOf()
    var isDeleted: Boolean = false
}
