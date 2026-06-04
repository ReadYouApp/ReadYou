package me.ash.reader.ui.page.home.flow

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.ash.reader.ui.page.home.reading.PullToLoadDefaults
import me.ash.reader.ui.page.home.reading.PullToLoadState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.PullToSyncIndicator(
    pullToLoadState: PullToLoadState,
    modifier: Modifier = Modifier,
    isSyncing: Boolean,
) {
    val hapticFeedback = LocalHapticFeedback.current

    val animateOffsetFraction = remember { Animatable(if (isSyncing) 1f else 0f) }
    val animateAlpha = remember { Animatable(if (isSyncing) 1f else 0f) }
    val animateScale = remember { Animatable(if (isSyncing) 1f else .2f) }

    val progressFlow = remember(pullToLoadState) { snapshotFlow { pullToLoadState.progress } }

    val offsetFractionFlow =
        remember(pullToLoadState) { snapshotFlow { pullToLoadState.offsetFraction } }

    var showIndeterminateIndicator by remember { mutableStateOf(isSyncing) }

    val isSyncingFlow = snapshotFlow { isSyncing }

    val offsetSpec = remember {
        spring<Float>(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    }

    val scaleSpec = remember {
        spring<Float>(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    }

    val alphaSpec = remember {
        spring<Float>(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        )
    }

    LaunchedEffect(isSyncingFlow) {
        isSyncingFlow.collect { isSyncing ->
            if (isSyncing) {
                showIndeterminateIndicator = true
                animateAlpha.snapTo(1f)
                animateScale.snapTo(1f)
                launch {
                    animateOffsetFraction.animateTo(0f, offsetSpec)
                }
            } else {
                animateAlpha.animateTo(0f, alphaSpec)
                animateScale.snapTo(0f)
                showIndeterminateIndicator = false
            }
        }
    }

    LaunchedEffect(progressFlow) {
        progressFlow.collect { progress ->
            if (!showIndeterminateIndicator) {
                animateScale.snapTo(progress.fastCoerceAtMost(1f))
                animateAlpha.snapTo(progress.fastCoerceAtMost(1f))
            }
        }
    }

    LaunchedEffect(offsetFractionFlow) {
        offsetFractionFlow.collect {
            if (!showIndeterminateIndicator) {
                animateOffsetFraction.snapTo(it.fastCoerceAtMost(3f))
            }
        }
    }

    LaunchedEffect(progressFlow) {
        progressFlow.map { it > 1f }.distinctUntilChanged().collect {
            if (it && !showIndeterminateIndicator) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            }
        }
    }


    val fraction by remember { derivedStateOf { animateOffsetFraction.value } }


    Surface(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 72.dp)
            .align(Alignment.TopCenter)
            .offset {
                IntOffset(
                    x = 0,
                    y = (fraction * PullToLoadDefaults.ContentOffsetMultiple).dp.roundToPx()
                )
            }
            .graphicsLayer {
                this.alpha = animateAlpha.value
                this.scaleX = animateScale.value
                this.scaleY = animateScale.value
            }
            .size(48.dp),
        color = MaterialTheme.colorScheme.primaryFixedDim,
        shape = MaterialTheme.shapes.extraLarge) {
        Box(
            modifier = Modifier, contentAlignment = Alignment.Center
        ) {
            if (showIndeterminateIndicator) {
                val scale = remember { Animatable(1f) }
                LaunchedEffect(Unit) {
                    scale.animateTo(1.2f, animationSpec = scaleSpec)
                }
                LoadingIndicator(
                    color = MaterialTheme.colorScheme.onPrimaryFixedVariant,
                    modifier = Modifier
                        .size(38.dp)
                        .scale(scale.value)
                )
            } else {
                LoadingIndicator(
                    progress = { fraction },
                    color = MaterialTheme.colorScheme.onPrimaryFixedVariant,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}