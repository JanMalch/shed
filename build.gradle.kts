// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlinx.bcv)
    alias(libs.plugins.publish) apply false
    alias(libs.plugins.dokka)
}

// ./gradlew dokkaGenerate to generate docs for entire project
dokka {
    moduleName.set("Shed")
    moduleVersion.set(property("version") as String)
}

// https://kotlinlang.org/docs/dokka-migration.html#update-documentation-aggregation-in-multi-module-projects
dependencies {
    dokka(project(":shed"))
    dokka(project(":shed-nop"))
}

apiValidation {
    ignoredProjects += "app"
}
