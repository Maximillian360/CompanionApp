package ph.edu.auf.realmdiscussionbarebones.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import ph.edu.auf.realmdiscussionbarebones.RealmDiscussionApplication

object SharedPreferenceManager {
    private const val PREFERENCE_NAME = "AppPreferences"

    private val sharedPreferences : SharedPreferences by lazy {
        RealmDiscussionApplication.context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun putString(key: String, value : String){
        sharedPreferences.edit().putString(key,value).apply()
    }

    fun getString(key:String, default: String): String{
        return sharedPreferences.getString(key,default) ?: default
    }

    fun removeString(key:String){
        sharedPreferences.edit().remove(key).apply()
    }
}