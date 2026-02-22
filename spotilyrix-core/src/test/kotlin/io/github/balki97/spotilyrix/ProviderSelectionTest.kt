package io.github.balki97.spotilyrix

import io.github.balki97.spotilyrix.providers.Lrclib
import io.github.balki97.spotilyrix.providers.NetEase
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


