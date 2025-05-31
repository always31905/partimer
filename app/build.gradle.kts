plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.haedal_project"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.haedal_project"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    // ① 먼저 BOM
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))

    // ② Firebase modules
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    // ③ Firestore 추가
    implementation("com.google.firebase:firebase-firestore-ktx")
    // Firebase realtime_db
    implementation("com.google.firebase:firebase-database-ktx")

    // Google Sign-In (Gmail 로그인용)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

}
