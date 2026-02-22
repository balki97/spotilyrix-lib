rootProject.name = "spotilyrix-lib"

include(":spotilyrix-lib-core", ":spotilyrix-lib-cli")

project(":spotilyrix-lib-core").projectDir = file("spotilyrix-core")
project(":spotilyrix-lib-cli").projectDir = file("spotilyrix-cli")


