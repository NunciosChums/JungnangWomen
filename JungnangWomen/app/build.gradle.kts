import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.hilt)
  alias(libs.plugins.ksp)
}

android {
  namespace = "kr.susemi99.jungnangwomen"
  compileSdk = 37

  defaultConfig {
    applicationId = "kr.susemi99.jungnangwomen"
    minSdk = 26
    targetSdk = 37
    versionCode = 20
    versionName = "8"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    buildConfigField("String", "API_KEY", readProperty("API_KEY"))
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
}

fun readProperty(key: String): String {
  val file = rootProject.file("local.properties")
  val props = Properties().apply { if (file.exists()) file.inputStream().use { load(it) } }
  return props.getProperty(key) ?: error("local.properties 에 $key 가 없습니다")
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.material.icons.extended)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)

  // api
  implementation(libs.retrofit)
  implementation(libs.retrofit.json.converter)
  implementation(libs.retrofit.logging.interceptor)

  // json
  implementation(libs.kotlinx.serialization.json)

  // logcat
  implementation(libs.timber)

  // view model
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // pagination
  implementation(libs.androidx.paging.compose)

  // hilt
  implementation(libs.hilt.android)
  ksp(libs.hilt.android.compiler)
  implementation(libs.androidx.hilt.navigation.compose)
}
