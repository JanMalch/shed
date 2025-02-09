plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("shed.dokka")
    id("shed.publish")
}

android {
    namespace = "io.github.janmalch.shed"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles += getDefaultProguardFile("proguard-android-optimize.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    add("implementation", libs.findLibrary("timber").get())
    add("api", libs.findLibrary("kotlinx.datetime").get())
    add("api", libs.findLibrary("kotlinx.coroutines.android").get())
}
