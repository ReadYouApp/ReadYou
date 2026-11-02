package me.ash.reader.ui.component.webview

import org.junit.Assert
import org.junit.Test

class WebViewHtmlTest {

    @Test
    fun testResolveBaseUrl() {
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl(null))
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl(""))
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl("   "))
        Assert.assertEquals(
            "https://example.com/article",
            WebViewHtml.resolveBaseUrl("https://example.com/article"),
        )
        Assert.assertEquals(
            "https://example.com/article",
            WebViewHtml.resolveBaseUrl("//example.com/article"),
        )
        Assert.assertEquals(
            "https://example.com/article",
            WebViewHtml.resolveBaseUrl("example.com/article"),
        )
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl("https://example.com/a b"))
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl("javascript:alert(1)"))
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl("file:///sdcard/article.html"))
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl("file:/etc/passwd"))
    }

    @Test
    fun testEscapeAttribute() {
        Assert.assertEquals(
            "https://example.com/?a=1&amp;b=&quot;2&quot;",
            WebViewHtml.escapeAttribute("https://example.com/?a=1&b=\"2\""),
        )
        Assert.assertEquals(
            "&lt;script&gt;",
            WebViewHtml.escapeAttribute("<script>"),
        )
    }
}
