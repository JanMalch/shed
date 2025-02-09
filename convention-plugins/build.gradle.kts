plugins {
    `kotlin-dsl`
}

dependencies {
    // add plugins here, to use them in the plugins {} block
    implementation(libs.android.library.plugin)
    implementation(libs.kotlin.android.plugin)
    implementation(libs.publish.plugin)
    implementation(libs.dokka.plugin)
}
