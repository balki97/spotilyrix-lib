plugins {
    kotlin("jvm")
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
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
