package io.github.balki97.spotilyrix.cli

import io.github.balki97.spotilyrix.search
import java.util.logging.Level
import java.util.logging.Logger

private data class CliArgs(
    val searchTerm: String,
    val providers: List<String>,
    val lang: String?,
    val output: String,
    val verbose: Boolean,
    val plainOnly: Boolean,
    val syncedOnly: Boolean,
    val enhanced: Boolean,
)

fun cliHandler(rawArgs: Array<String>) {
    val args = parseArgs(rawArgs) ?: run {
        printUsage()
        return
    }

    if (args.verbose) {
        Logger.getLogger("").level = Level.FINE
    }

    val lrc = search(
        searchTerm = args.searchTerm,
        plainOnly = args.plainOnly,
        syncedOnly = args.syncedOnly,
        savePath = args.output,
        providers = args.providers,
        lang = args.lang,
        enhanced = args.enhanced,
    )

    if (!lrc.isNullOrBlank()) {
        println(lrc)
    }
}

private fun parseArgs(rawArgs: Array<String>): CliArgs? {
    if (rawArgs.isEmpty()) return null

    val providers = mutableListOf<String>()
    var lang: String? = null
    var output = "{search_term}.lrc"
    var verbose = false
    var plainOnly = false
    var syncedOnly = false
    var enhanced = false
    var searchTerm: String? = null

    var i = 0
    while (i < rawArgs.size) {
        when (val arg = rawArgs[i]) {
            "-p" -> {
                i++
                while (i < rawArgs.size && !rawArgs[i].startsWith("-")) {
                    providers.add(rawArgs[i].lowercase())
                    i++
                }
                continue
            }
            "-l", "--lang" -> {
                if (i + 1 >= rawArgs.size) return null
                lang = rawArgs[++i]
            }
            "-o", "--output" -> {
                if (i + 1 >= rawArgs.size) return null
                output = rawArgs[++i]
            }
            "-v", "--verbose" -> verbose = true
            "--plain-only" -> plainOnly = true
            "--synced-only" -> syncedOnly = true
            "--enhanced" -> enhanced = true
            else -> {
                if (arg.startsWith("-")) return null
                searchTerm = if (searchTerm == null) arg else "$searchTerm $arg"
            }
        }
        i++
    }

    val term = searchTerm?.trim().orEmpty()
    if (term.isBlank()) return null

    return CliArgs(
        searchTerm = term,
        providers = providers,
        lang = lang,
        output = output,
        verbose = verbose,
        plainOnly = plainOnly,
        syncedOnly = syncedOnly,
        enhanced = enhanced,
    )
}

private fun printUsage() {
    println("Search for LRC format (synchronized lyrics) of a music")
    println("Usage: spotilyrix-cli <search_term> [options]")
    println("Options:")
    println("  -p <providers...>     Providers: netease musixmatch lrclib")
    println("  -l, --lang <lang>     Translation language (Musixmatch only)")
    println("  -o, --output <path>   Save output .lrc file path (default: {search_term}.lrc)")
    println("  -v, --verbose         Enable verbose logs")
    println("  --plain-only          Only look for plain text lyrics")
    println("  --synced-only         Only look for synced lyrics")
    println("  --enhanced            Return word-by-word synced lyrics if available")
}


