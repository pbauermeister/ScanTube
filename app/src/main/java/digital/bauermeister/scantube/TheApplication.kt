package digital.bauermeister.scantube

import android.app.Application
import android.content.Context

fun getAppContext(): Context = theAppContext!!

private var theAppContext: Context? = null

class TheApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        theAppContext = this
    }
}