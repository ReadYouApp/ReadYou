package me.ash.reader.ui.page.settings.feedranking

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.ui.component.FeedIcon
import me.ash.reader.ui.component.base.Banner
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.theme.palette.onLight
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun FeedRankingPage(
    viewModel: FeedRankingViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val feeds = viewModel.feeds.collectAsStateValue()
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromKey = from.key as? String ?: return@rememberReorderableLazyListState
        val toKey = to.key as? String ?: return@rememberReorderableLazyListState
        val current = viewModel.feeds.value
        val fromIdx = current.indexOfFirst { it.id == fromKey }
        val toIdx = current.indexOfFirst { it.id == toKey }
        if (fromIdx >= 0 && toIdx >= 0) viewModel.reorder(fromIdx, toIdx)
    }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack,
            )
        },
        content = {
            LazyColumn(state = lazyListState) {
                item(key = "header") {
                    DisplayText(text = stringResource(R.string.feed_ranking), desc = "")
                }
                item(key = "hint") {
                    Banner(
                        title = stringResource(R.string.feed_ranking_hint),
                        desc = stringResource(R.string.feed_ranking_hint_desc),
                        icon = Icons.Outlined.Lightbulb,
                    )
                    Spacer(modifier = Modifier.padding(bottom = 8.dp))
                }
                itemsIndexed(feeds, key = { _, feed -> feed.id }) { index, feed ->
                    ReorderableItem(reorderState, key = feed.id) { isDragging ->
                        Surface(
                            tonalElevation = if (isDragging) 8.dp else 0.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(28.dp),
                                )
                                FeedIcon(
                                    feedName = feed.name,
                                    iconUrl = feed.icon,
                                    size = 24.dp,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = feed.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Outlined.DragHandle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.draggableHandle(),
                                )
                            }
                        }
                    }
                }
                item(key = "bottom") {
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        },
    )
}
