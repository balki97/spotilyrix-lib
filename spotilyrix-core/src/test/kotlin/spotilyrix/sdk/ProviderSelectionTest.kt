package spotilyrix.sdk

import spotilyrix.sdk.providers.Lrclib
import spotilyrix.sdk.providers.NetEase
import kotlin.test.Test
import kotlin.test.assertEquals

class ProviderSelectionTest {
    @Test
    fun `select providers by lowercase name`() {
        val providers = listOf(Lrclib(), NetEase())
        val selected = SpotiLyrix.selectProviders(providers, listOf("netease"))
        assertEquals(1, selected.size)
        assertEquals("NetEase", selected[0].toString())
    }
}





