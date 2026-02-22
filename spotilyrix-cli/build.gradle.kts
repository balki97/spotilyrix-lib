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

dependencies {
    implementation(project(":spotilyrix-core"))
}

application {
    mainClass.set("io.github.balki97.spotilyrix.cli.MainKt")
}


