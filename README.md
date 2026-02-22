# SpotiLyrix Lib (Kotlin)

Kotlin/Java port of the Python `syncedlyrics` library for easier Android and JVM integration.

## Modules

- `spotilyrix-lib-core`: reusable Kotlin/JVM library API.
- `spotilyrix-lib-cli`: command-line entrypoint equivalent to Python `cli` + `__main__`.

## Core API

```kotlin
import io.github.balki97.spotilyrix.search

val lrc = search("My Song My Artist")
```

```java
import io.github.balki97.spotilyrix.SpotiLyrix;

String lrc = SpotiLyrix.search("My Song My Artist");
```

### Search Options

- `plainOnly`: only return plaintext lyrics
- `syncedOnly`: only return synced lyrics
- `savePath`: output file path, supports `{search_term}` placeholder
- `providers`: provider filter list (`netease`, `musixmatch`, `lrclib`)
- `lang`: translation language (Musixmatch only)
- `enhanced`: word-by-word sync where available (Musixmatch only)

Default behavior returns synced lyrics only (unless `plainOnly=true`).

Default provider order is tuned for speed and synced hit rate:
`netease -> musixmatch -> lrclib`

Current speed ranking (one-song verification on February 22, 2026):
`1) netease (~1.8s) 2) musixmatch (~3.0s) 3) lrclib (~17.4s)`

## CLI

```bash
./gradlew :spotilyrix-lib-cli:run --args="Your Song Artist --synced-only"
```

Options mirror upstream Python project:

- `-p <providers...>`
- `-l, --lang <lang>`
- `-o, --output <path>`
- `-v, --verbose`
- `--plain-only`
- `--synced-only`
- `--enhanced`

## Provider Coverage

Ported providers and project pieces:

- Core API (`__init__`) -> `SpotiLyrix.search(...)` + top-level `search(...)`
- CLI (`cli.py`) -> `spotilyrix-lib-cli` module parser/handler
- Main entry (`__main__.py`) -> `spotilyrix-lib-cli` `main(...)`
- Utilities (`utils.py`) -> `Utils.kt`, `Lyrics.kt`, `TargetType.kt`
- Providers (`providers/*`) -> Kotlin providers in `spotilyrix-lib-core`

Notes:

- `Spotify` remains intentionally unimplemented, matching upstream TODO status.
- Non-working providers (`megalobiz`, `genius`, `deezer`, `lyricsify`) are removed from active selection.

## Publishing

You can publish this for reuse by other projects/users.

Quickest path: GitHub + JitPack

1. Push this repository to GitHub (for example `balki97/spotilyrix-lib`).
2. Create a release tag (for example `v1.0.0`).
3. In consumer projects, add JitPack repository:

```kotlin
repositories {
    maven("https://jitpack.io")
}
```

4. Add dependency:

```kotlin
dependencies {
    implementation("com.github.balki97:spotilyrix-lib-core:v1.0.0")
}
```

CLI artifact (optional):

```kotlin
dependencies {
    implementation("com.github.balki97:spotilyrix-lib-cli:v1.0.0")
}
```


