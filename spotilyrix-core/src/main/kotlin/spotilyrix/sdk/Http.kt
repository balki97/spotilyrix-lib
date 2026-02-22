package spotilyrix.sdk

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

internal object HttpClientFactory {
    fun defaultClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .callTimeout(12, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()
    }
}

private val defaultHeaders = mapOf(
    "User-Agent" to "spotilyrix-kotlin/1.0",
    "Accept" to "application/json, text/plain, */*",
)

internal fun buildUrl(baseUrl: String, params: List<Pair<String, String>> = emptyList()): HttpUrl {
    val builder = checkNotNull(baseUrl.toHttpUrlOrNullSafe()) { "Invalid URL: $baseUrl" }.newBuilder()
    params.forEach { (k, v) -> builder.addQueryParameter(k, v) }
    return builder.build()
}

internal fun OkHttpClient.get(
    url: String,
    params: List<Pair<String, String>> = emptyList(),
    headers: Map<String, String> = emptyMap(),
): Response {
    val requestBuilder = Request.Builder().url(buildUrl(url, params))
    defaultHeaders.forEach { (k, v) -> requestBuilder.header(k, v) }
    headers.forEach { (k, v) -> requestBuilder.header(k, v) }
    return executeWithRetry(requestBuilder.build())
}

internal fun OkHttpClient.getAbsolute(url: String, headers: Map<String, String> = emptyMap()): Response {
    val requestBuilder = Request.Builder().url(url)
    defaultHeaders.forEach { (k, v) -> requestBuilder.header(k, v) }
    headers.forEach { (k, v) -> requestBuilder.header(k, v) }
    return executeWithRetry(requestBuilder.build())
}

internal fun OkHttpClient.post(
    url: String,
    params: List<Pair<String, String>> = emptyList(),
    headers: Map<String, String> = emptyMap(),
    jsonBody: JSONObject? = null,
): Response {
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val bodyText = jsonBody?.toString() ?: "{}"
    val body = bodyText.toRequestBody(mediaType)

    val requestBuilder = Request.Builder().url(buildUrl(url, params)).post(body)
    defaultHeaders.forEach { (k, v) -> requestBuilder.header(k, v) }
    headers.forEach { (k, v) -> requestBuilder.header(k, v) }
    return executeWithRetry(requestBuilder.build())
}

private fun String.toHttpUrlOrNullSafe(): HttpUrl? {
    return this.toHttpUrlOrNull()
}

private fun OkHttpClient.executeWithRetry(request: Request, maxAttempts: Int = 3): Response {
    var attempt = 0
    var backoffMs = 100L
    var lastError: IOException? = null

    while (attempt < maxAttempts) {
        try {
            val response = newCall(request).execute()
            val shouldRetry = response.code == 429 || response.code in 500..599
            if (!shouldRetry || attempt == maxAttempts - 1) {
                return response
            }
            response.close()
        } catch (e: IOException) {
            lastError = e
            if (attempt == maxAttempts - 1) {
                throw e
            }
        }

        Thread.sleep(backoffMs)
        backoffMs *= 2
        attempt++
    }

    throw lastError ?: IOException("HTTP request failed after retries")
}





