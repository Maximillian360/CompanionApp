package ph.edu.auf.realmdiscussionbarebones

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class RealmDiscussionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context : Context
            private set
    }
}