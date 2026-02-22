rootProject.name = "spotilyrix-sdk"

include(":spotilyrix-sdk-core", ":spotilyrix-sdk-cli")

project(":spotilyrix-sdk-core").projectDir = file("spotilyrix-core")
project(":spotilyrix-sdk-cli").projectDir = file("spotilyrix-cli")