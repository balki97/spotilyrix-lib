package io.github.balki97.spotilyrix

import io.github.balki97.spotilyrix.providers.LrcProvider
import io.github.balki97.spotilyrix.providers.Providers
import java.util.logging.Logger

object SpotiLyrix {
    private val logger: Logger = Logger.getLogger(SpotiLyrix::class.java.name)

    @JvmStatic
    @JvmOverloads
    fun search(
        searchTerm: String,
        plainOnly: Boolean = false,
        syncedOnly: Boolean = false,
        savePath: String? = null,
        providers: List<String> = emptyList(),
        lang: String? = null,
        enhanced: Boolean = false,
    ): String? {
        if (plainOnly && syncedOnly) {
            logger.severe("--plain-only and --synced-only flags cannot be used together.")
            return null
        }

        val targetType = when {
            plainOnly -> TargetType.PLAINTEXT
            else -> TargetType.SYNCED_ONLY
        }

        val lyrics = Lyrics()
        val selectedProviders = if (providers.isEmpty()) {
            Providers.defaultProviders(lang = lang, enhanced = enhanced)
        } else {
            val requested = Providers.fromNames(providers, lang = lang, enhanced = enhanced)
            if (requested.isEmpty()) {
                logger.severe("Providers $providers not found in the list of available providers.")
                return null
            }
            requested
        }

        for (provider in selectedProviders) {
            logger.fine("Looking for an LRC on $provider")
            try {
                lyrics.update(provider.getLrc(searchTerm))
            } catch (e: Exception) {
                logger.severe("An error occurred while searching for an LRC on $provider")
                logger.severe(e.message ?: e.toString())
                if (!lang.isNullOrBlank()) {
                    logger.severe("Aborting, since `lang` is only supported by Musixmatch")
                    return null
                }
                continue
            }

            when {
                lyrics.isPreferred(targetType) -> {
                    logger.info("Lyrics found for \"$searchTerm\" on $provider")
                    break
                }
                lyrics.isAcceptable(targetType) -> {
                    logger.info("Found plaintext lyrics on $provider, but continuing search for synced lyrics")
                }
                else -> logger.fine("No suitable lyrics found on $provider, continuing search...")
            }
        }

        if (!lyrics.isAcceptable(targetType)) {
            logger.info("No suitable lyrics found for \"$searchTerm\" :(")
            return null
        }

        savePath?.let {
            val outputPath = it.replace("{search_term}", searchTerm)
            lyrics.saveLrcFile(outputPath, targetType)
        }

        return lyrics.toOutput(targetType)
    }

    @JvmStatic
    fun selectProviders(providers: List<LrcProvider>, requestedNames: List<String>): List<LrcProvider> {
        val lowered = requestedNames.map { it.lowercase() }
        val selected = providers.filter { lowered.contains(it.toString().lowercase()) }

        if (selected.isEmpty()) {
            if (requestedNames.isNotEmpty()) {
                logger.severe("Providers $requestedNames not found in the list of available providers.")
                return emptyList()
            }
            return providers
        }

        return selected
    }
}


