package me.ash.reader.infrastructure.rss

import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.shortId
import me.ash.reader.ui.ext.dollarLast
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleReaderIdTest {
    @Test
    fun testParse() {
        assertEquals(
            "tag:google.com,2005:reader/item/5d0cfa30041d4348".shortId,
            "6705009029382226760",
        )

        assertEquals(
            "tag:google.com,2005:reader/item/024025978b5e50d2".shortId,
            "162170919393841362",
        )

        assertEquals(
            "tag:google.com,2005:reader/item/fb115bd6d34a8e9f".shortId,
            "-355401917359550817",
        )
    }

    @Test
    fun testConvert() {
        assertEquals("17$128849041293".dollarLast(), "128849041293")
    }
}
