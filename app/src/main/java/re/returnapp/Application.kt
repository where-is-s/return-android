package re.returnapp

import android.app.Application

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        Collection.init(this)
    }
}