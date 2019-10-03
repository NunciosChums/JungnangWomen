package kr.susemi99.jungnangwomen.application

import android.app.Application
import android.util.Log
import com.uber.rxdogtag.RxDogTag
import io.reactivex.plugins.RxJavaPlugins

class App : Application() {
  companion object {
    lateinit var instance: App
      private set
  }

  override fun onCreate() {
    super.onCreate()
    instance = this

    RxDogTag.install()
    RxJavaPlugins.setErrorHandler { Log.w("APP#", it) }
  }
}