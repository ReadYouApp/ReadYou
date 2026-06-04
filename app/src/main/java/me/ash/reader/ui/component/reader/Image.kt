package me.ash.reader.ui.component.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import coil.size.pxOrElse
import kotlin.math.abs
import kotlin.math.roundToInt
import me.ash.reader.ui.ext.extractDomain
import org.jsoup.helper.StringUtil
import org.jsoup.nodes.Element

@Composable
internal fun ArticleImage(
    modifier: Modifier = Modifier,
    data: Any?,
    contentDescription: String? = null,
    fillMaxWidth: Boolean,
    contentPadding: PaddingValues,
    shape: Shape,
    onClick: (() -> Unit)? = null,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val maxImageSize = maxImageSize()
        ArticleImage(
            modifier = modifier,
            data = data,
            contentDescription = contentDescription,
            fillMaxWidth = fillMaxWidth,
            contentPadding = contentPadding,
            size = maxImageSize,
            shape = shape,
            onClick = onClick,
        )
    }
}

@Composable
internal fun ArticleImage(
    modifier: Modifier = Modifier,
    imageCandidates: ImageCandidates,
    contentDescription: String? = null,
    fillMaxWidth: Boolean,
    contentPadding: PaddingValues,
    shape: Shape,
    onImageClick: ((String, String) -> Unit)? = null,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val maxImageSize = maxImageSize()
        val imgUrl =
            imageCandidates.getBestImageForMaxSize(
                pixelDensity = LocalDensity.current.density,
                maxSize = maxImageSize,
            )
        val onClick =
            if (onImageClick == null) null
            else {
                { onImageClick(imgUrl, contentDescription ?: "") }
            }
        ArticleImage(
            modifier = modifier,
            data = imgUrl,
            contentDescription = contentDescription,
            fillMaxWidth = fillMaxWidth,
            contentPadding = contentPadding,
            size = maxImageSize,
            shape = shape,
            onClick = onClick,
        )
    }
}

@Composable
private fun ArticleImage(
    modifier: Modifier = Modifier,
    data: Any?,
    contentDescription: String? = null,
    fillMaxWidth: Boolean,
    contentPadding: PaddingValues,
    shape: Shape,
    size: Size,
    onClick: (() -> Unit)? = null,
) {
    val painter =
        rememberAsyncImagePainter(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .apply {
                        val domain = data.toString().extractDomain()
                        if (data.toString().extractDomain() != null) {
                            addHeader("Referer", domain!!)
                        }
                        data(data = data)
                        crossfade(true)
                        precision(Precision.INEXACT)
                        size(size)
                    }
                    .build()
        )
    Image(
        painter = painter,
        contentDescription = contentDescription,
        contentScale = if (fillMaxWidth) ContentScale.FillWidth else ContentScale.Inside,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(contentPadding)
                .clip(shape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    }
                ),
    )
    when (painter.state) {
        is AsyncImagePainter.State.Error,
        is AsyncImagePainter.State.Loading -> {
            Spacer(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .padding(contentPadding)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow, shape)
            )
        }

        else -> {}
    }
}

@Composable
private fun BoxWithConstraintsScope.maxImageSize() =
    with(LocalDensity.current) {
        val maxWidthPx = maxWidth.toPx().roundToInt()

        Size(
            width = maxWidth.toPx().roundToInt().coerceAtLeast(1),
            height = maxHeight.toPx().roundToInt().coerceAtLeast(1).coerceAtMost(10 * maxWidthPx),
        )
    }

/** Gets the url to the image in the <img> tag - could be from srcset or from src */
internal fun getImageSource(baseUrl: String, element: Element) =
    ImageCandidates(
        baseUrl = baseUrl,
        srcSet = element.attr("srcset") ?: "",
        absSrc = element.attr("abs:src") ?: "",
    )

internal class ImageCandidates(val baseUrl: String, val srcSet: String, val absSrc: String) {

    val hasImage: Boolean = srcSet.isNotBlank() || absSrc.isNotBlank()

    /** Might throw if hasImage returns false */
    fun getBestImageForMaxSize(maxSize: Size, pixelDensity: Float): String {
        val setCandidate =
            srcSet
                .splitToSequence(", ")
                .map { it.trim() }
                .map { it.split(SpaceRegex).take(2).map { x -> x.trim() } }
                .fold(100f to "") { acc, candidate ->
                    val candidateSize =
                        if (candidate.size == 1) {
                            // Assume it corresponds to 1x pixel density
                            1.0f / pixelDensity
                        } else {
                            val descriptor = candidate.last()
                            when {
                                descriptor.endsWith("w", ignoreCase = true) -> {
                                    (descriptor.substringBefore("w").toFloatOrNull() ?: return@fold acc) /
                                        maxSize.width.pxOrElse { 1 }
                                }

                                descriptor.endsWith("x", ignoreCase = true) -> {
                                    (descriptor.substringBefore("x").toFloatOrNull() ?: return@fold acc) / pixelDensity
                                }

                                else -> {
                                    return@fold acc
                                }
                            }
                        }

                    if (abs(candidateSize - 1.0f) < abs(acc.first - 1.0f)) {
                        candidateSize to candidate.first()
                    } else {
                        acc
                    }
                }
                .second

        return StringUtil.resolve(baseUrl, setCandidate.takeIf { it.isNotBlank() } ?: absSrc)
    }
}

private val SpaceRegex = Regex("\\s+")
