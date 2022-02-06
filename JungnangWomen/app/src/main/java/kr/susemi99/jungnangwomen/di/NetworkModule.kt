package kr.susemi99.jungnangwomen.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.plus
import kr.susemi99.jungnangwomen.BuildConfig
import kr.susemi99.jungnangwomen.api.Api
import kr.susemi99.jungnangwomen.util.serializer.LocalDateSerializer
import kr.susemi99.jungnangwomen.util.serializer.LocalTimeSerializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
  @Provides
  @Singleton
  fun providesOkHttpClient() = OkHttpClient.Builder().apply {
    if (BuildConfig.DEBUG) {
      addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    }
  }.build()

  @Provides
  @Singleton
  fun provideConverterFactory(): Json {
    return Json {
      ignoreUnknownKeys = true
      encodeDefaults = true
      isLenient = true

      serializersModule += SerializersModule {
        contextual(LocalDateSerializer)
        contextual(LocalTimeSerializer)
      }
    }
  }

  @Provides
  fun provideUrl(): String {
    return "http://openapi.seoul.go.kr:8088/${BuildConfig.requestKey}/json/SeoulJungNangWomenResourcesClass/"
  }

  @Provides
  @Singleton
  fun provideRetrofit(okHttpClient: OkHttpClient, json: Json, url: String): Retrofit =
    Retrofit.Builder().apply {
      addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      baseUrl(url)
      client(okHttpClient)
    }.build()


  @Provides
  @Singleton
  fun provideApiService(retrofit: Retrofit): Api = retrofit.create(Api::class.java)
}