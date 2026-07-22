import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
}

val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}

val apiBaseUrl = (providers.gradleProperty("API_BASE_URL").orNull
    ?: localProperties.getProperty("API_BASE_URL")
    ?: "https://ruta-libre.onrender.com/api/")
    .trim()
    .let { if (it.endsWith('/')) it else "$it/" }

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "mx.utng.cala.core"
    compileSdk {
        version = release(36) { minorApiLevel = 1 }
    }

    defaultConfig {
        minSdk = 24
        buildConfigField("String", "API_BASE_URL", apiBaseUrl.asBuildConfigString())
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)
    implementation(libs.hivemq.mqtt.client)
}
