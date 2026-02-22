package spotilyrix.sdk.providers

import spotilyrix.sdk.Lyrics
import spotilyrix.sdk.get
import spotilyrix.sdk.getBestMatch
import org.json.JSONArray
import org.json.JSONObject

class NetEase : LrcProvider() {
    companion object {
        private const val API_ENDPOINT_METADATA = "https://music.163.com/api/search/pc"
        private const val API_ENDPOINT_LYRICS = "https://music.163.com/api/song/lyric"

        private const val COOKIE = "NMTID=00OAVK3xqDG726ITU6jopU6jF2yMk0AAAGCO8l1BA; " +
            "JSESSIONID-WYYY=8KQo11YK2GZP45RMlz8Kn80vHZ9%2FGvwzRKQXXy0iQoFKycWdBlQjbfT0MJrFa6hwRfmpfBYKeHliUPH287JC3hNW99WQjrh9b9RmKT%2Fg1Exc2VwHZcsqi7ITxQgfEiee50po28x5xTTZXKoP%2FRMctN2jpDeg57kdZrXz%2FD%2FWghb%5C4DuZ%3A1659124633932; " +
            "_iuqxldmzr_=32; _ntes_nnid=0db6667097883aa9596ecfe7f188c3ec,1659122833973; " +
            "_ntes_nuid=0db6667097883aa9596ecfe7f188c3ec; WNMCID=xygast.1659122837568.01.0; " +
            "WEVNSM=1.0.0; WM_NI=CwbjWAFbcIzPX3dsLP%2F52VB%2Bxr572gmqAYwvN9KU5X5f1nRzBYl0SNf%2BV9FTmmYZy%2FoJLADaZS0Q8TrKfNSBNOt0HLB8rRJh9DsvMOT7%2BCGCQLbvlWAcJBJeXb1P8yZ3RHA%3D; " +
            "WM_NIKE=9ca17ae2e6ffcda170e2e6ee90c65b85ae87b9aa5483ef8ab3d14a939e9a83c459959caeadce47e991fbaee82af0fea7c3b92a81a9ae8bd64b86beadaaf95c9cedac94cf5cedebfeb7c121bcaefbd8b16dafaf8fbaf67e8ee785b6b854f7baff8fd1728287a4d1d246a6f59adac560afb397bbfc25ad9684a2c76b9a8d00b2bb60b295aaafd24a8e91bcd1cb4882e8beb3c964fb9cbd97d04598e9e5a4c6499394ae97ef5d83bd86a3c96f9cbeffb1bb739aed9ea9c437e2a3; " +
            "WM_TID=AAkRFnl03RdABEBEQFOBWHCPOeMra4IL; playerid=94262567"
    }

    private var referer: String? = null

    private fun searchTrack(searchTerm: String): JSONObject? {
        val params = listOf(
            "limit" to "10",
            "type" to "1",
            "offset" to "0",
            "s" to searchTerm,
        )
        val headers = buildMap {
            put("cookie", COOKIE)
            referer?.let { put("referer", it) }
        }

        return client.get(API_ENDPOINT_METADATA, params = params, headers = headers).use { response ->
            if (!response.isSuccessful) return null
            referer = response.request.url.toString()
            val root = JSONObject(response.body?.string() ?: return null)
            val songs = root.optJSONObject("result")?.optJSONArray("songs") ?: JSONArray()
            val results = mutableListOf<JSONObject>()
            for (i in 0 until songs.length()) {
                songs.optJSONObject(i)?.let(results::add)
            }
            if (results.isEmpty()) return null

            getBestMatch(results, searchTerm, compareKey = {
                val name = it.optString("name")
                val artist = it.optJSONArray("artists")?.optJSONObject(0)?.optString("name").orEmpty()
                "$name $artist".trim()
            })
        }
    }

    override fun getLrcById(trackId: String): Lyrics? {
        val params = listOf("id" to trackId, "lv" to "1")
        val headers = buildMap {
            put("cookie", COOKIE)
            referer?.let { put("referer", it) }
        }

        return client.get(API_ENDPOINT_LYRICS, params = params, headers = headers).use { response ->
            if (!response.isSuccessful) return null
            val root = JSONObject(response.body?.string() ?: return null)
            val lrcText = root.optJSONObject("lrc")?.optString("lyric", null)
            return Lyrics().apply { addUnknown(lrcText) }
        }
    }

    override fun getLrc(searchTerm: String): Lyrics? {
        val track = searchTrack(searchTerm) ?: return null
        val id = track.opt("id")?.toString() ?: return null
        return getLrcById(id)
    }
}




