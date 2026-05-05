import org.jetbrains.kotlin.buildtools.api.arguments.enums.JvmTarget
import org.jetbrains.kotlin.gradle.internal.builtins.StandardNames.FqNames.target

plugins {
   alias(libs.plugins.androidApplication)
   alias(libs.plugins.composeMultiplatform)
   alias(libs.plugins.composeCompiler)
}

dependencies {
   implementation(projects.composeApp)
   implementation(libs.compose.uiToolingPreview)
   implementation(libs.androidx.activity.compose)
   implementation(libs.ktor.client.okhttp)
}

android {
   namespace = "com.server.notatki"
   compileSdk = libs.versions.android.compileSdk.get().toInt()

   defaultConfig {
      applicationId = "com.server.notatki"
      minSdk = libs.versions.android.minSdk.get().toInt()
      targetSdk = libs.versions.android.targetSdk.get().toInt()
      versionCode = 1
      versionName = "1.0"
   }
   packaging {
      resources {
         excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
   }
   buildTypes {
      getByName("release") {
         isMinifyEnabled = false
         signingConfig = signingConfigs.getByName("debug")
      }
   }
   compileOptions {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11
   }
}