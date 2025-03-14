plugins {
    id("shed.lib")
}

description = "Content provider to setup Shed automatically."

dependencies {
    implementation(project(":shed"))
}