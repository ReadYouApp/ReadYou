package me.ash.reader.ui.page.home.feeds

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.model.group.GroupWithFeed
import me.ash.reader.ui.page.home.feeds.drawer.group.GroupOptionViewModel
import me.ash.reader.ui.theme.Shape32
import me.ash.reader.ui.theme.ShapeTop32

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GroupItem(
    group: Group,
    isExpanded: () -> Boolean,
    groupOptionViewModel: GroupOptionViewModel = hiltViewModel(),
    onExpanded: () -> Unit = {},
    onLongClick: () -> Unit = {},
    groupOnClick: () -> Unit = {},
) {
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    groupOnClick()
                },
                onLongClick = {
                    groupOptionViewModel.fetchGroup(groupId = group.id)
                    onLongClick()
                }
            )
            .padding(top = 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 28.dp),
                text = group.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier
                    .padding(end = 20.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { onExpanded() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (isExpanded()) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = stringResource(if (isExpanded()) R.string.expand_less else R.string.expand_more),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
    }
}

@Composable
inline fun GroupWithFeedsContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
            .clip(Shape32)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        content = { content() }
    )
}
