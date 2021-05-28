package kr.susemi99.jungnangwomen.application

import android.app.Application
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import kr.susemi99.jungnangwomen.util.Logg
import rxdogtag2.RxDogTag

class App : Application() {
  companion object {
    lateinit var instance: App
      private set
  }

  override fun onCreate() {
    super.onCreate()
    instance = this

    RxDogTag.install()
    RxJavaPlugins.setErrorHandler { Logg.w(it) }
  }
}