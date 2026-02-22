package io.github.balki97.spotilyrix.providers

import io.github.balki97.spotilyrix.Lyrics
import io.github.balki97.spotilyrix.formatTime
import io.github.balki97.spotilyrix.get
import io.github.balki97.spotilyrix.getBestMatch
import io.github.balki97.spotilyrix.getCachePath
import io.github.balki97.spotilyrix.sortResults
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Musixmatch(
    private val lang: String? = null,
    private val enhanced: Boolean = false,
) : LrcProvider() {
    companion object {
        private const val ROOT_URL = "https://apic-desktop.musixmatch.com/ws/1.1/"
        private const val COOLDOWN_MS = 120_000L
        @Volatile
        private var blockedUntilMs: Long = 0L
    }

    private var token: String? = null

    private fun isInCooldown(): Boolean = System.currentTimeMillis() < blockedUntilMs

    private fun activateCooldown() {
        blockedUntilMs = System.currentTimeMillis() + COOLDOWN_MS
    }

    private fun get(action: String, query: MutableList<Pair<String, String>>): JSONObject? {
        if (action != "token.get" && isInCooldown()) {
            logger.fine("Musixmatch is in cooldown window; skipping request")
            return null
        }
        if (action != "token.get" && token == null) {
            getToken() ?: return null
        }
        query.add("app_id" to "web-desktop-app-v1.0")
        token?.let { query.add("usertoken" to it) }
        query.add("t" to System.currentTimeMillis().toString())

        val json = client.get(ROOT_URL + action, params = query).use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            JSONObject(body)
        }
        val status = json.optJSONObject("message")
            ?.optJSONObject("header")
            ?.optInt("status_code", -1) ?: -1

        // Token can become invalid between calls; invalidate and let caller retry.
        if (action != "token.get" && status == 401) {
            token = null
            activateCooldown()
        }
        return json
    }

    private fun getToken(): String? {
        val tokenPath = File(getCachePath("spotilyrix", false), "musixmatch_token.json")
        val nowSeconds = (System.currentTimeMillis() / 1000L).toInt()

        if (tokenPath.exists()) {
            runCatching {
                val cached = JSONObject(tokenPath.readText())
                val cachedToken = cached.optString("token", "")
                val expiration = cached.optInt("expiration_time", 0)
                if (cachedToken.isNotBlank() && nowSeconds < expiration) {
                    token = cachedToken
                    return cachedToken
                }
            }
        }

        var delayMs = 2_000L
        repeat(8) {
            val response = get("token.get", mutableListOf("user_language" to "en"))
            val status = response?.optJSONObject("message")
                ?.optJSONObject("header")
                ?.optInt("status_code", -1) ?: -1
            val newToken = response?.optJSONObject("message")
                ?.optJSONObject("body")
                ?.optString("user_token", null)

            if (status == 200 && !newToken.isNullOrBlank()) {
                val expiration = (System.currentTimeMillis() / 1000L).toInt() + 600
                val tokenData = JSONObject()
                    .put("token", newToken)
                    .put("expiration_time", expiration)
                tokenPath.parentFile?.mkdirs()
                tokenPath.writeText(tokenData.toString())
                token = newToken
                return newToken
            }

            Thread.sleep(delayMs)
            delayMs = (delayMs * 2).coerceAtMost(60_000L)
        }

        logger.warning("Failed to obtain Musixmatch token after retries")
        token = null
        activateCooldown()
        return null
    }

    override fun getLrcById(trackId: String): Lyrics? {
        val subtitleResponse = fetchWithReauth(
            action = "track.subtitle.get",
            query = mutableListOf(
                "track_id" to trackId,
                "subtitle_format" to "lrc",
            ),
        ) ?: return null

        var translations: JSONArray? = null
        if (!lang.isNullOrBlank()) {
            val trResponse = fetchWithReauth(
                action = "crowd.track.translations.get",
                query = mutableListOf(
                    "track_id" to trackId,
                    "subtitle_format" to "lrc",
                    "translation_fields_set" to "minimal",
                    "selected_language" to lang,
                ),
            ) ?: return null

            translations = trResponse.optJSONObject("message")
                ?.optJSONObject("body")
                ?.optJSONArray("translations_list")
            if (translations == null || translations.length() == 0) {
                throw IllegalStateException("Couldn't find translations")
            }
        }

        val body = subtitleResponse.optJSONObject("message")?.optJSONObject("body") ?: return null
        val subtitleBody = body.optJSONObject("subtitle")?.optString("subtitle_body", null) ?: return null
        var lrc = subtitleBody

        translations?.let {
            for (i in 0 until it.length()) {
                val tr = it.getJSONObject(i).optJSONObject("translation") ?: continue
                val original = tr.optString("subtitle_matched_line")
                val translated = tr.optString("description")
                lrc = lrc.replace(original, "$original\n($translated)")
            }
        }

        return Lyrics(synced = lrc)
    }

    fun getLrcWordByWord(trackId: String): Lyrics? {
        val response = fetchWithReauth(
            action = "track.richsync.get",
            query = mutableListOf("track_id" to trackId),
        ) ?: return null
        val status = response.optJSONObject("message")
            ?.optJSONObject("header")
            ?.optInt("status_code", -1) ?: -1
        if (status != 200) return null

        val richsyncBody = response.optJSONObject("message")
            ?.optJSONObject("body")
            ?.optJSONObject("richsync")
            ?.optString("richsync_body", null)
            ?: return null

        val richsync = JSONArray(richsyncBody)
        val builder = StringBuilder()
        for (i in 0 until richsync.length()) {
            val line = richsync.getJSONObject(i)
            val ts = line.optDouble("ts", 0.0)
            builder.append("[").append(formatTime(ts)).append("] ")
            val words = line.optJSONArray("l") ?: JSONArray()
            for (w in 0 until words.length()) {
                val word = words.getJSONObject(w)
                val offset = word.optDouble("o", 0.0)
                val text = word.optString("c", "")
                val t = formatTime(ts + offset)
                builder.append("<").append(t).append("> ").append(text).append(" ")
            }
            builder.append("\n")
        }

        return Lyrics(synced = builder.toString())
    }

    override fun getLrc(searchTerm: String): Lyrics? {
        val response = fetchWithReauth(
            "track.search",
            mutableListOf(
                "q" to searchTerm,
                "page_size" to "5",
                "page" to "1",
            ),
        ) ?: return null

        val statusCode = response.optJSONObject("message")
            ?.optJSONObject("header")
            ?.optInt("status_code", -1) ?: -1
        if (statusCode != 200) {
            logger.warning("Got status code $statusCode for $searchTerm")
            return null
        }

        val trackList = response.optJSONObject("message")
            ?.optJSONObject("body")
            ?.optJSONArray("track_list")
            ?: return null

        val tracks = mutableListOf<JSONObject>()
        for (i in 0 until trackList.length()) {
            val item = trackList.optJSONObject(i) ?: continue
            tracks.add(item)
        }
        if (tracks.isEmpty()) return null

        val sortedTracks = sortResults(
            tracks,
            searchTerm,
            compareKey = {
                val t = it.optJSONObject("track") ?: JSONObject()
                "${t.optString("track_name")} ${t.optString("artist_name")}".trim()
            },
        )

        val track = getBestMatch(
            sortedTracks,
            searchTerm,
            compareKey = {
                val t = it.optJSONObject("track") ?: JSONObject()
                "${t.optString("track_name")} ${t.optString("artist_name")}".trim()
            },
        )

        // Try best match first, then fall back to other top candidates.
        val candidates = buildList {
            bestMatchTrack(track)?.let { add(it) }
            sortedTracks
                .mapNotNull(::bestMatchTrack)
                .filterNot { id -> contains(id) }
                .take(4)
                .forEach { add(it) }
        }
        for (trackId in candidates) {
            if (enhanced) {
                val enhancedLyrics = getLrcWordByWord(trackId)
                if (!enhancedLyrics?.synced.isNullOrBlank()) return enhancedLyrics
            }
            val subtitleLyrics = getLrcById(trackId)
            if (!subtitleLyrics?.synced.isNullOrBlank()) return subtitleLyrics
        }

        return null
    }

    private fun bestMatchTrack(trackItem: JSONObject?): String? {
        return trackItem
            ?.optJSONObject("track")
            ?.opt("track_id")
            ?.toString()
            ?.takeIf { it.isNotBlank() }
    }

    private fun fetchWithReauth(action: String, query: MutableList<Pair<String, String>>): JSONObject? {
        val first = get(action, query.toMutableList()) ?: return null
        val firstStatus = first.optJSONObject("message")
            ?.optJSONObject("header")
            ?.optInt("status_code", -1) ?: -1
        if (firstStatus != 401) return first

        token = null
        getToken() ?: return first
        return get(action, query.toMutableList())
    }
}


