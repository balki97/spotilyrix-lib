package io.github.balki97.spotilyrix.providers

object Providers {
    @JvmStatic
    fun defaultProviders(lang: String? = null, enhanced: Boolean = false): List<LrcProvider> {
        return listOf(
            NetEase(),
            Musixmatch(lang = lang, enhanced = enhanced),
            Lrclib(),
        )
    }

    @JvmStatic
    fun allProviders(lang: String? = null, enhanced: Boolean = false): List<LrcProvider> {
        return listOf(
            NetEase(),
            Musixmatch(lang = lang, enhanced = enhanced),
            Lrclib(),
        )
    }

    @JvmStatic
    fun fromNames(
        names: List<String>,
        lang: String? = null,
        enhanced: Boolean = false,
    ): List<LrcProvider> {
        val lowered = names.map { it.lowercase() }.toSet()
        return allProviders(lang = lang, enhanced = enhanced)
            .filter { lowered.contains(it.toString().lowercase()) }
    }
}


