# SpotiLyrix SDK (Kotlin)

Kotlin/Java library for fetching synchronized song lyrics on JVM and Android projects.

## Modules

- `spotilyrix-sdk-core`: reusable Kotlin/JVM library API.
- `spotilyrix-sdk-cli`: command-line entrypoint.

## Core API

```kotlin
import spotilyrix.sdk.search

val lrc = search("My Song My Artist")
```

```java
import spotilyrix.sdk.SpotiLyrix;

String lrc = SpotiLyrix.search("My Song My Artist");
```

### Search Options

- `plainOnly`: only return plaintext lyrics
- `syncedOnly`: only return synced lyrics
- `savePath`: output file path, supports `{search_term}` placeholder
- `providers`: provider filter list (`netease`, `musixmatch`, `lrclib`)
- `lang`: translation language (Musixmatch only)
- `enhanced`: word-by-word sync where available (Musixmatch only)

Default behavior prefers synced lyrics and falls back to plaintext only when needed.

Default provider order is tuned for speed and synced hit rate:
`netease -> musixmatch -> lrclib`

## CLI

```bash
./gradlew :spotilyrix-sdk-cli:run --args="Your Song Artist --synced-only"
```

CLI options:

- `-p <providers...>`
- `-l, --lang <lang>`
- `-o, --output <path>`
- `-v, --verbose`
- `--plain-only`
- `--synced-only`
- `--enhanced`

## Publish For Reuse (JitPack)

This repository is prepared for JitPack.

### 1) Push and tag

```bash
git add .
git commit -m "Prepare SDK for JitPack"
git push origin main
git tag v1.0.0
git push origin v1.0.0
```

### 2) Verify build on JitPack

Open:

`https://jitpack.io/#balki97/SyncedLyrics-Android/v1.0.0`

### 3) Use in Android/JVM projects

Add repository:

```kotlin
repositories {
    maven("https://jitpack.io")
}
```

Add dependency:

```kotlin
dependencies {
    implementation("com.github.balki97.SyncedLyrics-Android:spotilyrix-sdk-core:v1.0.0")
}
```

If JitPack shows a different coordinate on the package page, use exactly that coordinate.
