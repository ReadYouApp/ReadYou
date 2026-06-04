package me.ash.reader.ui.page.home.reading

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalReadingPageTonalElevation
import me.ash.reader.infrastructure.preference.LocalReadingRenderer
import me.ash.reader.infrastructure.preference.ReadingPageTonalElevationPreference
import me.ash.reader.infrastructure.preference.ReadingRendererPreference
import me.ash.reader.ui.component.base.CanBeDisabledIconButton
import me.ash.reader.ui.component.webview.BoldCharactersIcon

private val sizeSpec = spring<IntSize>(stiffness = 700f)

@Composable
fun BottomBar(
    isShow: Boolean,
    isUnread: Boolean,
    isStarred: Boolean,
    isNextArticleAvailable: Boolean,
    isFullContent: Boolean,
    isBoldCharacters: Boolean,
    ttsButton: @Composable () -> Unit,
    onUnread: (isUnread: Boolean) -> Unit = {},
    onStarred: (isStarred: Boolean) -> Unit = {},
    onNextArticle: () -> Unit = {},
    onFullContent: (isFullContent: Boolean) -> Unit = {},
    onBoldCharacters: () -> Unit = {},
    onReadAloud: () -> Unit = {},
) {
    val tonalElevation = LocalReadingPageTonalElevation.current
    val isOutlined = tonalElevation == ReadingPageTonalElevationPreference.Outlined
    val renderer = LocalReadingRenderer.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isShow,
            enter = expandVertically(expandFrom = Alignment.Top, animationSpec = sizeSpec),
            exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = sizeSpec)
        ) {
            val view = LocalView.current
            Column {
                if (isOutlined) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        thickness = 0.5f.dp
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.run { if (isOutlined) surface else surfaceContainer }
                ) {
                    // TODO: Component styles await refactoring
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .fillMaxWidth()
                            .height(60.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CanBeDisabledIconButton(
                            modifier = Modifier.size(40.dp),
                            disabled = false,
                            imageVector = if (isUnread) {
                                Icons.Filled.FiberManualRecord
                            } else {
                                Icons.Outlined.FiberManualRecord
                            },
                            contentDescription = stringResource(if (isUnread) R.string.mark_as_read else R.string.mark_as_unread),
                            tint = if (isUnread) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onUnread(!isUnread)
                        }
                        CanBeDisabledIconButton(
                            modifier = Modifier.size(40.dp),
                            disabled = false,
                            imageVector = if (isStarred) {
                                Icons.Rounded.Star
                            } else {
                                Icons.Rounded.StarOutline
                            },
                            contentDescription = stringResource(if (isStarred) R.string.mark_as_unstar else R.string.mark_as_starred),
                            tint = if (isStarred) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onStarred(!isStarred)
                        }
                        CanBeDisabledIconButton(
                            disabled = !isNextArticleAvailable,
                            modifier = Modifier.size(40.dp),
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = "Next Article",
                            tint = MaterialTheme.colorScheme.outline,
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onNextArticle()
                        }
                        ttsButton()
                        CanBeDisabledIconButton(
                            disabled = false,
                            modifier = Modifier.size(40.dp),
                            imageVector = if (isFullContent) {
                                Icons.AutoMirrored.Rounded.Article
                            } else {
                                Icons.AutoMirrored.Outlined.Article
                            },
                            contentDescription = stringResource(R.string.parse_full_content),
                            tint = if (isFullContent) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onFullContent(!isFullContent)
                        }
                    }
                }
            }
        }
    }
}
