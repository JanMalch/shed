@file:JvmName("Projects")

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

// TODO: https://github.com/ktorio/ktor/blob/main/build-logic/src/main/kotlin/ktorbuild/internal/VersionCatalogs.kt#L15
// internal, so it's only for conventions
internal val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
