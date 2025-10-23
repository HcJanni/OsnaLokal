import java.util.Properties

val properties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.osnalokal"
    compileSdk {
        version = release(36)
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.osnalokal"
        minSdk = 35
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // DIESE ZEILE ist der Befehl, die BuildConfig-Variable zu erstellen
        buildConfigField(
            "String",
            "GOOGLE_MAPS_API_KEY",
            properties.getProperty("GOOGLE_MAPS_API_KEY", "\"\"")
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.maps:google-maps-services:2.2.0")  {
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation(libs.transportation.consumer){
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}