package me.ash.reader.ui.component.webview

import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import me.ash.reader.infrastructure.preference.LocalOpenLink
import me.ash.reader.infrastructure.preference.LocalOpenLinkSpecificBrowser
import me.ash.reader.infrastructure.preference.LocalReadingBoldCharacters
import me.ash.reader.infrastructure.preference.LocalReadingFonts
import me.ash.reader.infrastructure.preference.LocalReadingImageHorizontalPadding
import me.ash.reader.infrastructure.preference.LocalReadingImageRoundedCorners
import me.ash.reader.infrastructure.preference.LocalReadingPageTonalElevation
import me.ash.reader.infrastructure.preference.LocalReadingSubheadBold
import me.ash.reader.infrastructure.preference.LocalReadingSubheadUpperCase
import me.ash.reader.infrastructure.preference.LocalReadingTextAlign
import me.ash.reader.infrastructure.preference.LocalReadingTextBold
import me.ash.reader.infrastructure.preference.LocalReadingTextFontSize
import me.ash.reader.infrastructure.preference.LocalReadingTextHorizontalPadding
import me.ash.reader.infrastructure.preference.LocalReadingTextLetterSpacing
import me.ash.reader.infrastructure.preference.LocalReadingTextLineHeight
import me.ash.reader.infrastructure.preference.ReadingFontsPreference
import me.ash.reader.ui.ext.ExternalFonts
import me.ash.reader.ui.ext.openURL
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.theme.palette.alwaysLight

@Composable
fun RYWebView(
    modifier: Modifier = Modifier,
    content: String,
    refererDomain: String? = null,
    onImageClick: ((imgUrl: String, altText: String) -> Unit)? = null,
) {
    val context = LocalContext.current
    val maxWidth = LocalConfiguration.current.screenWidthDp.dp.value
    val openLink = LocalOpenLink.current
    val openLinkSpecificBrowser = LocalOpenLinkSpecificBrowser.current
    val tonalElevation = LocalReadingPageTonalElevation.current
    val backgroundColor =
        MaterialTheme.colorScheme.surfaceColorAtElevation(tonalElevation.value.dp).toArgb()
    val selectionTextColor = Color.Black.toArgb()
    val selectionBgColor = (MaterialTheme.colorScheme.tertiaryContainer alwaysLight true).toArgb()
    val textColor: Int = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val textBold: Boolean = LocalReadingTextBold.current.value
    val textAlign: String = LocalReadingTextAlign.current.toTextAlignCSS()
    val textMargin: Int = LocalReadingTextHorizontalPadding.current
    val boldTextColor: Int = MaterialTheme.colorScheme.onSurface.toArgb()
    val linkTextColor: Int = MaterialTheme.colorScheme.primary.toArgb()
    val subheadBold: Boolean = LocalReadingSubheadBold.current.value
    val subheadUpperCase: Boolean = LocalReadingSubheadUpperCase.current.value
    val readingFonts = LocalReadingFonts.current
    val fontSize: Int = LocalReadingTextFontSize.current
    val letterSpacing: Float = LocalReadingTextLetterSpacing.current
    val lineHeight: Float = LocalReadingTextLineHeight.current
    val imgMargin: Int = LocalReadingImageHorizontalPadding.current
    val imgBorderRadius: Int = LocalReadingImageRoundedCorners.current
    val codeTextColor: Int = MaterialTheme.colorScheme.tertiary.toArgb()
    val codeBgColor: Int =
        MaterialTheme.colorScheme.surfaceColorAtElevation((tonalElevation.value + 6).dp).toArgb()
    val boldCharacters = LocalReadingBoldCharacters.current

    val webView by
        remember(backgroundColor) {
            mutableStateOf(
                WebViewLayout.get(
                    context = context,
                    readingFontsPreference = readingFonts,
                    webViewClient =
                        WebViewClient(
                            context = context,
                            refererDomain = refererDomain,
                            onOpenLink = { url ->
                                context.openURL(url, openLink, openLinkSpecificBrowser)
                            },
                        ),
                    onImageClick = onImageClick,
                )
            )
        }

    val fontPath =
        if (readingFonts is ReadingFontsPreference.External)
            ExternalFonts.FontType.ReadingFont.toPath(context)
        else if (readingFonts is ReadingFontsPreference.GoogleSans) {
            "/android_res/font/google_sans_flex.ttf"
        } else null

    AndroidView(
        modifier = modifier,
        factory = { webView },
        update = {
            it.apply {
                Log.i("RLog", "maxWidth: ${maxWidth}")
                Log.i("RLog", "readingFont: ${context.filesDir.absolutePath}")
                Log.i("RLog", "CustomWebView: ${content}")
                settings.defaultFontSize = fontSize
                loadDataWithBaseURL(
                    null,
                    WebViewHtml.HTML.format(
                        WebViewStyle.get(
                            fontSize = fontSize,
                            fontPath = fontPath,
                            lineHeight = lineHeight,
                            letterSpacing = letterSpacing,
                            textMargin = textMargin,
                            textColor = textColor,
                            textBold = textBold,
                            textAlign = textAlign,
                            boldTextColor = boldTextColor,
                            subheadBold = subheadBold,
                            subheadUpperCase = subheadUpperCase,
                            imgMargin = imgMargin,
                            imgBorderRadius = imgBorderRadius,
                            linkTextColor = linkTextColor,
                            codeTextColor = codeTextColor,
                            codeBgColor = codeBgColor,
                            tableMargin = textMargin,
                            selectionTextColor = selectionTextColor,
                            selectionBgColor = selectionBgColor,
                        ),
                        url,
                        content,
                        WebViewScript.get(boldCharacters.value),
                    ),
                    "text/HTML",
                    "UTF-8",
                    null,
                )
            }
        },
    )
}
