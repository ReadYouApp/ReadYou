package me.ash.reader.ui.page.home.feeds

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.ui.component.FeedIcon
import me.ash.reader.ui.component.base.RYExtensibleVisibility
import me.ash.reader.ui.page.home.feeds.drawer.feed.FeedOptionViewModel

@Composable
private fun contentPadding(isLastItem: Boolean): PaddingValues = if (isLastItem) PaddingValues(
    bottom = 22.dp, start = 14.dp, end = 14.dp, top = 14.dp
) else PaddingValues(14.dp)

@OptIn(
    ExperimentalFoundationApi::class,
)
@Composable
private fun FeedItemImpl(
    feed: Feed,
    isLastItem: () -> Boolean = { false },
    onLongClickCallback: (String) -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {
                onClick()
            }, onLongClick = {
                onLongClick()
                scope.launch {
                    onLongClickCallback(feed.id)
                }
            })
            .padding(contentPadding(isLastItem())),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                FeedIcon(
                    feedName = feed.name, iconUrl = feed.icon, modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp),
                    text = feed.name,
                    style = MaterialTheme.typography.labelLarge.merge(
                        lineHeight = 20.sp,
                        lineHeightStyle = LineHeightStyle(
                            trim = LineHeightStyle.Trim.Both,
                            alignment = LineHeightStyle.Alignment.Center
                        )
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (feed.important != 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.outline,
                    content = {
                        Text(
                            text = feed.important.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                )
            }
        }
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
)
@Composable
fun FeedItem(
    feed: Feed,
    isLastItem: () -> Boolean = { false },
    isExpanded: () -> Boolean,
    feedOptionViewModel: FeedOptionViewModel = hiltViewModel(),
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    RYExtensibleVisibility(visible = isExpanded()) {
        FeedItemImpl(
            feed = feed,
            isLastItem = isLastItem,
            onClick = onClick,
            onLongClick = onLongClick,
            onLongClickCallback = { feedId ->
                scope.launch {
                    feedOptionViewModel.fetchFeed(feedId = feedId)
                }
            })
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            FeedItemImpl(
                feed = Feed(
                    id = "1",
                    name = "Awesome Feed",
                    icon = null,
                    important = 5,
                    url = "https://example.com",
                    groupId = "1",
                    accountId = 1,
                    isNotification = false,
                    isFullContent = false,
                ),
            )
        }
    }

}