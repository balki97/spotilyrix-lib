plugins {
    kotlin("jvm")
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

base {
    archivesName.set("spotilyrix-sdk-cli")
}

dependencies {
    implementation(project(":spotilyrix-sdk-core"))
}

application {
    mainClass.set("spotilyrix.sdk.cli.MainKt")
}