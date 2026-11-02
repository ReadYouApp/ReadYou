package me.ash.reader.ui.component.webview

import java.net.URI
import me.ash.reader.ui.ext.formatUrl

object WebViewHtml {

    fun resolveBaseUrl(url: String?): String {
        val normalized = url?.trim()?.takeIf { it.isNotBlank() }?.formatUrl() ?: return ""
        val uri = runCatching { URI(normalized) }.getOrNull() ?: return ""
        val scheme = uri.scheme?.lowercase()
        return if ((scheme == "http" || scheme == "https") && uri.host != null) normalized else ""
    }

    fun escapeAttribute(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    const val HTML: String = """
<!DOCTYPE html>
<html dir="auto">
<head>
    <meta name="viewport" content="initial-scale=1, minimum-scale=1, maximum-scale=1, user-scalable=no, width=device-width, viewport-fit=cover" />
    <meta content="text/html; charset=utf-8" http-equiv="content-type"/>
    <style type="text/css">
        %s
    </style>
    <base href="%s" />
</head>
<body>
<main>
    <article>
        %s
    </article>
</main>
<script>
%s
</script>
</body>
</html>
"""
}
