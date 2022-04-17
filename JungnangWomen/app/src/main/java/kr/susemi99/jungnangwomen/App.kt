package kr.susemi99.jungnangwomen

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kr.susemi99.jungnangwomen.util.DebugLogTree
import timber.log.Timber

@HiltAndroidApp
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      Timber.plant(DebugLogTree())
    }
  }
}