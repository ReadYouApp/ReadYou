package me.ash.reader.ui.page.home.flow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import me.ash.reader.R
import me.ash.reader.domain.data.FilterState
import me.ash.reader.ui.page.home.reading.PullToLoadDefaults
import me.ash.reader.ui.page.home.reading.PullToLoadState
import me.ash.reader.ui.page.home.reading.PullToLoadState.Status.PulledUp
import me.ash.reader.ui.page.home.reading.PullToLoadState.Status.PullingUp
import kotlin.math.abs

sealed interface LoadAction {
    class NextFeed(val feedName: String) : LoadAction {
        companion object {
            fun fromFilterState(filterState: FilterState): NextFeed {
                val name = filterState.group?.name ?: filterState.feed?.name.toString()
                return NextFeed(name)
            }
        }
    }

    object MarkAllAsRead : LoadAction
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.PullToLoadIndicator(
    modifier: Modifier = Modifier,
    state: PullToLoadState,
    loadAction: LoadAction? = null,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val status = state.status

    val canLoadNext = loadAction != null

    LaunchedEffect(status) {
        when {
            canLoadNext && status == PulledUp -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            }

            else -> {}
        }
    }

    val alignment = Alignment.BottomCenter

    val visible = remember(status, canLoadNext) {
        when (status) {
            PullingUp, PulledUp -> {
                canLoadNext
            }

            else -> {
                false
            }
        }
    }

    if (visible && !state.isSettled) {
        Box(
            modifier = modifier
                .align(alignment)
                .padding(vertical = 80.dp)
        ) {
            AnimatedVisibility(
                visible = status == PulledUp,
                enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()) + scaleIn(
                    MaterialTheme.motionScheme.fastSpatialSpec(),
                    transformOrigin = TransformOrigin(.5f, 1f),
                    initialScale = .5f
                ),
                exit = fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec())
            ) {
                when (loadAction) {
                    LoadAction.MarkAllAsRead -> MarkAsReadIndicator()
                    is LoadAction.NextFeed -> NextFeedIndicator(feedName = loadAction.feedName)
                    null -> return@AnimatedVisibility
                }
            }
        }
    }
}

@Composable
private fun MarkAsReadIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.tertiaryFixed,
                shape = MaterialTheme.shapes.extraLarge
            ), contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 20.dp)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.DoneAll,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryFixed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.mark_all_as_read),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryFixed
            )
        }
    }
}


@Composable
private fun NextFeedIndicator(feedName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.secondaryFixed,
                shape = MaterialTheme.shapes.extraLarge
            ), contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 20.dp)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowDownward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryFixed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                feedName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryFixed
            )
        }
    }
}

@Preview
@Composable
private fun PreviewMarkAsRead() {
    MarkAsReadIndicator()
}

@Preview
@Composable
private fun PreviewNextFeed() {
    NextFeedIndicator("AndroidX Releases")
}