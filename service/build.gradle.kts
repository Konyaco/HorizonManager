plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlinx-serialization")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31
//        versionCode = 1
//        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        val debug by getting
        val release by getting {
            proguardFiles.apply {
                add(getDefaultProguardFile("proguard-android-optimize.txt"))
                add(file("proguard-rules.pro"))
            }
        }
        val beta by creating {
            initWith(release)
            proguardFiles.apply {
                add(getDefaultProguardFile("proguard-android-optimize.txt"))
                add(file("proguard-rules.pro"))
            }
        }
        val alpha by creating {
            initWith(release)
            proguardFiles.apply {
                add(getDefaultProguardFile("proguard-android-optimize.txt"))
                add(file("proguard-rules.pro"))
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
}