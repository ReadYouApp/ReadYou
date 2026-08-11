package me.ash.reader.ui.component.base

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
fun AnimatedPopup(
    visible: Boolean = false,
    absoluteY: Dp = Dp.Hairline,
    absoluteX: Dp = Dp.Hairline,
    onDismissRequest: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val density = LocalDensity.current
    val statusBarsHeight = WindowInsets.statusBars.getTop(density)

    Popup(
        properties = PopupProperties(focusable = visible),
        onDismissRequest = onDismissRequest,
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                return IntOffset(
                    x = with(density) { (absoluteX).roundToPx() },
                    y = with(density) { (absoluteY).roundToPx() + statusBarsHeight }
                )
            }
        },
    ) {
        RYExtensibleVisibility(visible = visible) {
            content()
        }
    }
}
