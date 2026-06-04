package me.ash.reader.ui.motion

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import kotlin.math.roundToInt

enum class Direction {
    Backward, Forward
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun sharedYAxisTransitionExpressive(direction: Direction): ContentTransform {
    val direction = when (direction) {
        Direction.Backward -> -1
        Direction.Forward -> 1
    }
    val exit = 150
    val enter = exit * 2
    return (slideInVertically(
        initialOffsetY = { (it / 2 * direction).toInt() },
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
    ) + fadeIn(
        tween(
            delayMillis = exit,
            durationMillis = enter,
            easing = LinearOutSlowInEasing
        )
    )) togetherWith (slideOutVertically(
        targetOffsetY = { (it / -4 * direction).toInt() },
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()

    ) + fadeOut(
        tween(durationMillis = exit, easing = FastOutLinearInEasing)
    ))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun sharedYAxisTransitionSlow(direction: Direction, motionScheme: MotionScheme): ContentTransform {
    val direction = when (direction) {
        Direction.Backward -> -1
        Direction.Forward -> 1
    }
    val exit = 150
    val enter = exit * 2
    return (slideInVertically(
        initialOffsetY = { (it / 2 * direction).toInt() },
        animationSpec = motionScheme.slowSpatialSpec()
    ) + fadeIn(
        tween(
            delayMillis = exit,
            durationMillis = enter,
            easing = LinearOutSlowInEasing
        )
    )) togetherWith (slideOutVertically(
        targetOffsetY = { (it / -2 * direction).toInt() },
        animationSpec = motionScheme.slowSpatialSpec()

    ) + fadeOut(
        tween(durationMillis = exit, easing = FastOutLinearInEasing)
    ))
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun sharedXAxisTransitionSlow(direction: Direction): ContentTransform {
    val direction = when (direction) {
        Direction.Backward -> -1
        Direction.Forward -> 1
    }
    val animationDuration = 400
    val exit = (animationDuration * .35f).roundToInt()
    val enter = animationDuration - exit
    return (slideInHorizontally(
        initialOffsetX = { (it * 0.1f * direction).toInt() },
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
    ) + fadeIn(
        tween(
            delayMillis = exit,
            durationMillis = enter,
            easing = LinearOutSlowInEasing
        )
    )) togetherWith (slideOutHorizontally(
        targetOffsetX = { (it * -0.1f * direction).toInt() },
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
    ) + fadeOut(
        tween(durationMillis = exit, easing = FastOutLinearInEasing)
    ))
}