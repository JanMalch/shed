import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
    id("com.vanniktech.maven.publish")
}

publishing {
    repositories {
        maven {
            name = "ProjectBuildDir"
            url = uri(rootProject.layout.buildDirectory.dir("m2"))
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = project.property("group") as String,
        artifactId = project.name,
        version = project.property("version") as String,
    )

    pom {
        name.set(project.name)
        url.set("https://github.com/JanMalch/Shed/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("JanMalch")
                url.set("https://github.com/JanMalch/")
            }
        }
        scm {
            url.set("https://github.com/JanMalch/Shed/")
            connection.set("scm:git:git://github.com/JanMalch/Shed.git")
            developerConnection.set("scm:git:ssh://git@github.com/JanMalch/Shed.git")
        }
    }

    configure(AndroidSingleVariantLibrary(
        // the published variant
        variant = "release",
        // whether to publish a sources jar
        sourcesJar = true,
        // whether to publish a javadoc jar
        publishJavadocJar = true,
    ))

    publishToMavenCentral()

    signAllPublications()
}

afterEvaluate {
    mavenPublishing {
        pom {
            description.set(project.description.takeUnless { it.isNullOrBlank() }
                ?: throw IllegalStateException("Project '${project.name}' has no description set."))
        }
    }
}
