plugins {
    id("shed.lib")
}

description = "Content provider to setup Shed automatically."

android {
    namespace = "io.github.janmalch.shed.autoload"
}

dependencies {
    implementation(project(":shed"))
}
