package spotilyrix.sdk

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
    return SpotiLyrix.search(
        searchTerm = searchTerm,
        plainOnly = plainOnly,
        syncedOnly = syncedOnly,
        savePath = savePath,
        providers = providers,
        lang = lang,
        enhanced = enhanced,
    )
}





