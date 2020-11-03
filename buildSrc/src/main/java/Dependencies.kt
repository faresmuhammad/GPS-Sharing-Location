object Versions {
    const val kotlin = "1.3.72"
    const val material = "1.3.0-alpha01"
    const val dagger = "2.28.1"
    const val firebaseAuth = "20.0.0"
    const val firebaseFirestore = "21.5.0"
    const val firebaseAnalytics = "17.2.2"
    const val lifecycle = "2.2.0"
    const val countryCodePicker = "2.4.0"
    const val kotlinCore = "1.3.0"
    const val appcompat = "1.1.0"
    const val constraintlayout = "1.1.3"
    const val recyclerview = "1.1.0"
    const val junit = "4.13"
    const val junitandroidx = "1.1.1"
    const val espresso = "3.2.0"
    const val gradle = "4.0.1"
    const val google_services = "4.3.3"
    const val rxjava = "2.1.8"
    const val material_dialog = "3.3.0"
    const val google_maps = "2.1.1"
    const val google_services_location = "17.0.0"
}

object Deps {
    //Base
    const val gradle = "com.android.tools.build:gradle:${Versions.gradle}"
    const val kotlin_gradle_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val google_services = "com.google.gms:google-services:${Versions.google_services}"
    const val kotlinstdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val androidx_kotlincore = "androidx.core:core-ktx:${Versions.kotlinCore}"

    //Material Design
    const val material = "com.google.android.material:material:${Versions.material}"

    //Dagger
    const val dagger_runtime = "com.google.dagger:dagger:${Versions.dagger}"
    const val dagger_compiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    const val dagger_androidRuntime = "com.google.dagger:dagger-android:${Versions.dagger}"
    const val dagger_androidCompiler =
        "com.google.dagger:dagger-android-processor:${Versions.dagger}"
    const val dagger_supportLibrary = "com.google.dagger:dagger-android-support:${Versions.dagger}"


    //AndroidX
    const val androidx_app_compat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val androidx_constraintlayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}"
    const val androidx_recyclerview = "androidx.recyclerview:recyclerview:${Versions.recyclerview}"

    //Lifecycle and ViewModel
    const val androidx_lifecycle = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}"

    //Testing
    const val junit_test = "junit:junit:${Versions.junit}"
    const val androidx_junit = "androidx.test.ext:junit:${Versions.junitandroidx}"
    const val androidx_espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"

    //Firebase
    const val firebase_analytics =
        "com.google.firebase:firebase-analytics:${Versions.firebaseAnalytics}"
    const val firebase_auth = "com.google.firebase:firebase-auth-ktx:${Versions.firebaseAuth}"
    const val firebase_firestore =
        "com.google.firebase:firebase-firestore-ktx:${Versions.firebaseFirestore}"

    //Country Code Picker
    const val countryCodePicker = "com.hbb20:ccp:${Versions.countryCodePicker}"

    //Rx
    const val rxjava = "io.reactivex.rxjava2:rxjava:${Versions.rxjava}"

    //Material Dialog
    const val material_dialog = "com.afollestad.material-dialogs:core:${Versions.material_dialog}"
    const val material_dialog_input =
        "com.afollestad.material-dialogs:input:${Versions.material_dialog}"
    const val material_dialog_lifecycle =
        "com.afollestad.material-dialogs:lifecycle:${Versions.material_dialog}"

    //Google Maps
    const val google_maps = "com.google.maps.android:maps-ktx:${Versions.google_maps}"
    const val google_maps_utils = "com.google.maps.android:maps-utils-ktx:${Versions.google_maps}"
    const val google_services_location =
        "com.google.android.gms:play-services-location:${Versions.google_services_location}"

    const val timber = "com.jakewharton.timber:timber:4.7.1"

}