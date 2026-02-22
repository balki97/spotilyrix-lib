package io.github.balki97.spotilyrix

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilsTest {
    @Test
    fun `identify synced lyrics`() {
        val lrc = """
            [ar:artist]
            [ti:title]
            [al:album]
            [00:01.00] first
            [00:05.00] second
            [00:09.00] third
        """.trimIndent()

        assertEquals("synced", identifyLyricsType(lrc))
    }

    @Test
    fun `convert synced to plaintext`() {
        val lrc = "[00:01.00] Hello\n[00:02.00] World"
        assertEquals("Hello\nWorld", syncedToPlaintext(lrc))
    }

    @Test
    fun `string match score is case insensitive`() {
        assertTrue(strScore("Song Artist", "song artist") > 90)
    }
}


