package spotilyrix.sdk.providers

import spotilyrix.sdk.Lyrics
import spotilyrix.sdk.get
import spotilyrix.sdk.getAbsolute
import spotilyrix.sdk.sortResults
import org.json.JSONArray
import org.json.JSONObject

class Lrclib : LrcProvider() {
    companion object {
        private const val ROOT_URL = "https://lrclib.net"
        private const val API_ENDPOINT = "$ROOT_URL/api"
        private const val SEARCH_ENDPOINT = "$API_ENDPOINT/search"
        private const val LRC_ENDPOINT = "$API_ENDPOINT/get/"
    }

    override fun getLrcById(trackId: String): Lyrics? {
        return client.getAbsolute(LRC_ENDPOINT + trackId).use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val track = JSONObject(body)
            Lyrics(
                synced = track.optString("syncedLyrics").ifBlank { null },
                unsynced = track.optString("plainLyrics").ifBlank { null },
            )
        }
    }

    override fun getLrc(searchTerm: String): Lyrics? {
        val tracks = client.get(SEARCH_ENDPOINT, params = listOf("q" to searchTerm)).use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val arr = JSONArray(body)
            val list = mutableListOf<JSONObject>()
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let(list::add)
            }
            list
        }

        if (tracks.isEmpty()) return null

        val sorted = sortResults(tracks, searchTerm) {
            "${it.optString("artistName")} - ${it.optString("trackName")}".trim()
        }

        val id = sorted.firstOrNull()?.opt("id")?.toString() ?: return null
        return getLrcById(id)
    }
}




