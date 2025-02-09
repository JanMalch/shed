import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    id("org.jetbrains.dokka")
}

dokka {
    dokkaSourceSets.configureEach {
        includes.from("README.md")
        documentedVisibilities(VisibilityModifier.Public, VisibilityModifier.Protected)
        // suppressGeneratedFiles doesn't suppress generated Room files,
        // so we exclude them by filtering the source roots to actual source code.
        sourceRoots = sourceRoots.filter { "src" in it.path }

        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl("https://github.com/janmalch/shed/tree/main/${projectDir.name}/src")
            remoteLineSuffix.set("#L")
        }
    }
}
