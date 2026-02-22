package io.github.balki97.spotilyrix.providers

import io.github.balki97.spotilyrix.HttpClientFactory
import io.github.balki97.spotilyrix.Lyrics
import okhttp3.OkHttpClient
import java.util.logging.Logger

abstract class LrcProvider {
    protected val client: OkHttpClient = HttpClientFactory.defaultClient()
    protected val logger: Logger = Logger.getLogger(this::class.java.simpleName)

    override fun toString(): String = this::class.java.simpleName

    open fun getLrcById(trackId: String): Lyrics? = throw NotImplementedError()

    abstract fun getLrc(searchTerm: String): Lyrics?
}


