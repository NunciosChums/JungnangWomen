package kr.susemi99.jungnangwomen.di

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
import kr.susemi99.jungnangwomen.api.XmlErrorConverterFactory
import kr.susemi99.jungnangwomen.util.serializer.LocalDateSerializer
import kr.susemi99.jungnangwomen.util.serializer.LocalTimeSerializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
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
  fun provideJson(): Json {
    return Json {
      ignoreUnknownKeys = true
      isLenient = true

      serializersModule += SerializersModule {
        contextual(LocalDateSerializer)
        contextual(LocalTimeSerializer)
      }
    }
  }

  // TODO(#15): 서울 열린데이터광장 OpenAPI가 HTTPS를 제공하면 https://로 전환하고 network_security_config 제거
  private const val BASE_URL = "http://openapi.seoul.go.kr:8088/"

  @Provides
  @Singleton
  fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
    Retrofit.Builder().apply {
      addConverterFactory(XmlErrorConverterFactory())
      addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      baseUrl("$BASE_URL${BuildConfig.API_KEY}/json/SeoulJungNangWomenResourcesClass/")
      client(okHttpClient)
    }.build()


  @Provides
  @Singleton
  fun provideApiService(retrofit: Retrofit): Api = retrofit.create(Api::class.java)
}