package spotilyrix.sdk

data class HttpConfig(
    val connectTimeoutSeconds: Long = 2,
    val readTimeoutSeconds: Long = 6,
    val callTimeoutSeconds: Long = 7,
    val retryAttempts: Int = 2,
    val forceHttp1: Boolean = true,
)

data class MusixmatchConfig(
    val cooldownMillis: Long = 120_000L,
    val tokenMaxRetries: Int = 8,
    val tokenInitialBackoffMillis: Long = 2_000L,
    val tokenMaxBackoffMillis: Long = 60_000L,
    val searchCandidateLimit: Int = 5,
)

data class SpotiLyrixConfig(
    val http: HttpConfig = HttpConfig(),
    val musixmatch: MusixmatchConfig = MusixmatchConfig(),
)

