plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.finance.whatsapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.finance.whatsapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//        manifestPlaceholders["contentProviderAuthority"] = "$applicationId.stickercontentprovider"


        // Content Provider Authority
        val contentProviderAuthority = "$applicationId.stickercontentprovider"
        android {
            defaultConfig {
                manifestPlaceholders["contentProviderAuthority"] = contentProviderAuthority
                buildConfigField("String", "CONTENT_PROVIDER_AUTHORITY", "\"$contentProviderAuthority\"")
            }
        }

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures{
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.androidx.recyclerview)
    implementation (libs.core.ktx)

    //background remover
    implementation (libs.github.checkerboarddrawable)
    implementation ("com.github.dhaval2404:imagepicker:2.1")
    implementation ("com.github.AppIntro:AppIntro:6.3.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation (libs.gesture.views)
//    implementation (libs.easyimage)
    implementation (libs.recyclerview.v7)
//    implementation (libs.depth)

    implementation (libs.lottie)
    implementation ("io.github.sangcomz:fishbun:0.8.7")
    implementation (libs.glide)
//    implementation (libs.compiler)

    //Third party libraries
    implementation (libs.exifinterface)
    implementation (libs.fresco)
    implementation (libs.webpsupport)
    implementation (libs.animated.webp)
    implementation (libs.webpsupport.v1100)
    implementation (libs.gson)

    implementation (libs.core.ktx)
    implementation (libs.kotlin.stdlib.jdk7)
}
