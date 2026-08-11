package me.ash.reader.ui.ext

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.paging.compose.LazyPagingItems
import kotlin.math.abs

fun LazyListState.calculateTopBarAnimateValue(start: Float, end: Float): Float =
    if (firstVisibleItemIndex != 0) end
    else {
        val variable = firstVisibleItemScrollOffset.coerceAtLeast(0).toFloat()
        val duration = 256f
        val increase = abs(start - end) * (variable / duration)
        if (start < end) (start + increase).coerceIn(start, end)
        else (start - increase).coerceIn(end, start)
    }

@Composable
fun <T : Any> LazyPagingItems<T>.rememberLazyListState(): LazyListState {
    // After recreation, LazyPagingItems first return 0 items, then the cached items.
    // This behavior/issue is resetting the LazyListState scroll position.
    // Below is a workaround. More info: https://issuetracker.google.com/issues/177245496.
    return when (itemCount) {
        // Return a different LazyListState instance.
        0 -> remember(this) { LazyListState(0, 0) }
        // Return rememberLazyListState (normal case).
        else -> androidx.compose.foundation.lazy.rememberLazyListState()
    }
}

/**
 * TODO: To be improved
 *
 * Returns whether the LazyListState is currently in the
 * downward scrolling state.
 */
@Composable
fun LazyListState.isScrollDown(): Boolean {
    var isScrollDown by remember { mutableStateOf(false) }
    var preItemIndex by remember { mutableStateOf(0) }
    var preScrollStartOffset by remember { mutableStateOf(0) }

    LaunchedEffect(this) {
        snapshotFlow { isScrollInProgress }.collect {
            if (isScrollInProgress) {
                isScrollDown = when {
                    firstVisibleItemIndex > preItemIndex -> true
                    firstVisibleItemIndex < preItemIndex -> false
                    else -> firstVisibleItemScrollOffset > preScrollStartOffset
                }
            } else {
                preItemIndex = firstVisibleItemIndex
                preScrollStartOffset = firstVisibleItemScrollOffset
            }
        }
    }

    return isScrollDown
}