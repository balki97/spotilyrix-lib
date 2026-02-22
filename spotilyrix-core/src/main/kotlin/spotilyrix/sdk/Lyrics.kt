package spotilyrix.sdk

import java.io.File

data class Lyrics(
    var synced: String? = null,
    var unsynced: String? = null,
) {
    fun addUnknown(unknown: String?) {
        when (identifyLyricsType(unknown)) {
            "synced" -> synced = unknown
            "plaintext" -> unsynced = unknown
        }
    }

    fun update(other: Lyrics?) {
        if (other == null) return
        if (!other.synced.isNullOrBlank()) synced = other.synced
        if (!other.unsynced.isNullOrBlank()) unsynced = other.unsynced
    }

    fun isPreferred(targetType: TargetType): Boolean {
        return synced != null || (targetType == TargetType.PLAINTEXT && unsynced != null)
    }

    fun isAcceptable(targetType: TargetType): Boolean {
        return synced != null || (targetType != TargetType.SYNCED_ONLY && unsynced != null)
    }

    fun toOutput(targetType: TargetType): String? {
        return when (targetType) {
            TargetType.PLAINTEXT -> unsynced ?: synced?.let(::syncedToPlaintext)
            TargetType.PREFER_SYNCED -> synced ?: unsynced
            TargetType.SYNCED_ONLY -> synced
        }
    }

    fun saveLrcFile(path: String, targetType: TargetType) {
        val output = toOutput(targetType) ?: return
        File(path).writeText(output, Charsets.UTF_8)
    }
}





