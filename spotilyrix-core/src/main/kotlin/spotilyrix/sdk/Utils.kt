package spotilyrix.sdk

import me.xdrop.fuzzywuzzy.FuzzySearch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.time.Duration
import java.util.Locale

private val featRegex = Regex("\\((feat.+)\\)", RegexOption.IGNORE_CASE)
private val syncedPrefixRegex = Regex("\\[\\d+:\\d+\\.\\d+\\]\\s?")

fun getCachePath(libName: String = "spotilyrix", autoCreate: Boolean = true): File {
    val os = System.getProperty("os.name").lowercase(Locale.US)
    val userHome = System.getProperty("user.home")
    val baseDir = when {
        os.contains("win") -> System.getenv("LOCALAPPDATA") ?: userHome
        os.contains("mac") -> "$userHome/Library/Caches"
        else -> "$userHome/.cache"
    }

    val targetDir = File(baseDir, libName)
    if (autoCreate) {
        targetDir.mkdirs()
    }
    return targetDir
}

fun syncedToPlaintext(syncedLyrics: String): String {
    return syncedLyrics
        .lineSequence()
        .map { it.replace(syncedPrefixRegex, "") }
        .joinToString("\n")
}

fun identifyLyricsType(lrc: String?): String {
    if (lrc.isNullOrBlank()) return "invalid"
    val lines = lrc.split("\n")
    val sample = lines.drop(5).take(5)
    if (sample.isNotEmpty() && sample.all { it.contains("[") }) return "synced"
    return "plaintext"
}

fun hasTranslation(lrc: String): Boolean {
    val lines = lrc.split("\n").drop(5).take(5)
    lines.forEachIndexed { index, line ->
        if (line.contains("[") && index + 1 < lines.size) {
            val next = lines[index + 1]
            if (!next.contains("(")) return false
        }
    }
    return true
}

fun generateSoup(html: String): Document = Jsoup.parse(html)

fun formatTime(timeInSeconds: Double): String {
    val duration = Duration.ofMillis((timeInSeconds * 1000).toLong())
    val totalSeconds = duration.seconds
    val minutes = (totalSeconds / 60).toInt()
    val seconds = (totalSeconds % 60).toInt()
    val hundredths = ((duration.toMillis() % 1000) / 10).toInt()
    return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
}

fun strScore(a: String, b: String): Double {
    var left = a.lowercase(Locale.US)
    var right = b.lowercase(Locale.US)
    if (!right.contains("feat")) {
        left = left.replace(featRegex, "")
        right = right.replace(featRegex, "")
    }
    return FuzzySearch.tokenSetRatio(left, right).toDouble()
}

fun strSame(a: String, b: String, n: Int): Boolean = strScore(a, b).toInt() >= n

fun <T> sortResults(results: List<T>, searchTerm: String, compareKey: (T) -> String): List<T> {
    return results.sortedByDescending { strScore(compareKey(it), searchTerm) }
}

fun <T> getBestMatch(
    results: List<T>,
    searchTerm: String,
    compareKey: (T) -> String,
    minScore: Int = 65,
): T? {
    if (results.isEmpty()) return null
    val sorted = sortResults(results, searchTerm, compareKey)
    val best = sorted.first()
    val compareValue = compareKey(best)
    return if (strSame(compareValue, searchTerm, minScore)) best else null
}





