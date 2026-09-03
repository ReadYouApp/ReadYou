package me.ash.reader.ui.page.home.feeds

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.ash.reader.R
import me.ash.reader.domain.model.group.GroupWithFeed
import me.ash.reader.ui.theme.Shape32
import sh.calvin.reorderable.ReorderableColumn
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * 排序编辑模式：分组可整体拖拽排序，展开的分组内订阅源也可拖拽排序。
 * 拖拽结果由 [onDone] 一次性带回（group id 顺序 + 各组 feed id 顺序），由调用方持久化。
 */
@Composable
fun FeedsReorderMode(
    groupWithFeedList: List<GroupWithFeed>,
    onDone: (List<GroupWithFeed>) -> Unit,
) {
    var groups by remember {
        mutableStateOf(groupWithFeedList.map { it.copy(feeds = it.feeds.toMutableList()) })
    }
    val hapticFeedback = LocalHapticFeedback.current

    fun moveFeed(groupId: String, fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        groups =
            groups.map { gwf ->
                if (gwf.group.id == groupId) {
                    val feeds = gwf.feeds.toMutableList().apply {
                        add(toIndex, removeAt(fromIndex))
                    }
                    gwf.copy(feeds = feeds)
                } else {
                    gwf
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部工具条：提示 + 完成
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.DragHandle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.reorder_tips),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = { onDone(groups) }) {
                    Text(
                        text = stringResource(R.string.done),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        val lazyListState = rememberLazyListState()
        val reorderableLazyListState =
            rememberReorderableLazyListState(lazyListState) { from, to ->
                if (from.index != to.index) {
                    groups =
                        groups.toMutableList().apply {
                            add(to.index, removeAt(from.index))
                        }
                }
            }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
        ) {
            itemsIndexed(groups, key = { _, gwf -> gwf.group.id }) { _, gwf ->
                ReorderableItem(reorderableLazyListState, key = gwf.group.id) { isDragging ->
                    val elevation by animateDpAsState(
                        targetValue = if (isDragging) 8.dp else 0.dp,
                        label = "reorderGroupElevation",
                    )
                    GroupWithFeedsContainer(
                        modifier = Modifier.shadow(elevation, shape = Shape32, clip = false),
                    ) {
                        // 组头（可拖拽整组）
                        Column(modifier = Modifier.padding(top = 22.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .padding(horizontal = 28.dp),
                                    text = gwf.group.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                val groupHandleModifier = Modifier.draggableHandle()
                                Icon(
                                    imageVector = Icons.Rounded.DragHandle,
                                    contentDescription = stringResource(R.string.reorder),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier =
                                        groupHandleModifier
                                            .padding(end = 20.dp)
                                            .size(24.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(22.dp))
                        }

                        // 组内订阅源（展开态拖拽排序）
                        ReorderableColumn(
                            list = gwf.feeds,
                            onSettle = { fromIndex, toIndex ->
                                moveFeed(gwf.group.id, fromIndex, toIndex)
                                hapticFeedback.performHapticFeedback(
                                    HapticFeedbackType.SegmentFrequentTick
                                )
                            },
                        ) { feedIndex, feed, _ ->
                            key(feed.id) {
                                ReorderableItem {
                                    val feedHandleModifier = Modifier.draggableHandle()
                                    FeedItem(
                                        feed = feed,
                                        isLastItem = { feedIndex == gwf.feeds.lastIndex },
                                        isExpanded = { true },
                                        onClick = {},
                                        onLongClick = {},
                                        trailing = {
                                            Icon(
                                                imageVector = Icons.Rounded.DragHandle,
                                                contentDescription =
                                                    stringResource(R.string.reorder),
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier =
                                                    feedHandleModifier
                                                        .padding(start = 4.dp)
                                                        .size(18.dp),
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(128.dp))
            }
        }
    }
}
