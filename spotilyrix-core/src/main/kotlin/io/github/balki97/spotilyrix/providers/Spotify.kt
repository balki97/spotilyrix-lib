package io.github.balki97.spotilyrix.providers

import io.github.balki97.spotilyrix.Lyrics

class Spotify : LrcProvider() {
    companion object {
        @JvmStatic
        fun getTrackId(searchTerm: String): String {
            throw NotImplementedError("Spotify provider is not implemented yet")
        }
    }

    override fun getLrcById(trackId: String): Lyrics? {
        throw NotImplementedError("Spotify provider is not implemented yet")
    }

    override fun getLrc(searchTerm: String): Lyrics? {
        throw NotImplementedError("Spotify provider is not implemented yet")
    }
}


