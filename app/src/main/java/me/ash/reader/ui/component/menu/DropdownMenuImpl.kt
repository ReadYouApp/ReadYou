package me.ash.reader.ui.component.menu

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import me.ash.reader.domain.model.constant.ElevationTokens

private const val TAG = "DropdownMenuImpl"

class AnchorEndPopupPositionProvider(
    private val density: Density,
    private val offset: IntOffset,
    private val verticalMargin: Int = with(density) { DropdownMenuVerticalPadding.roundToPx() },
    private val onTransformOriginCalculated: (transformOrigin: TransformOrigin) -> Unit = { _ -> }
) : PopupPositionProvider {

    companion object {
        private val TopTransformOrigin = TransformOrigin(0.5f, 0f)

        private val BottomTransformOrigin = TransformOrigin(0.5f, 1f)
    }

    /**
     * Calculates the position of the popup.
     *
     * @param anchorBounds The window relative bounds of the anchor layout.
     * @param windowSize The size of the window containing the anchor.
     * @param layoutDirection The layout direction (LTR or RTL).
     * @param popupContentSize The size of the popup's content.
     * @return The calculated window relative position (IntOffset) for the popup.
     */
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {

        val popupX = offset.x - popupContentSize.width / 2

        val popupYPrimary = anchorBounds.bottom

        val fitsBelow = (popupYPrimary + popupContentSize.height) <= windowSize.height

        val popupYSecondary = anchorBounds.top - popupContentSize.height

        val popupY = if (fitsBelow) {
            popupYPrimary + verticalMargin
        } else {
            popupYSecondary - verticalMargin
        }

        onTransformOriginCalculated(if (fitsBelow) TopTransformOrigin else BottomTransformOrigin)


        return IntOffset(popupX, popupY)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DropdownMenuContent(
    expandedState: MutableTransitionState<Boolean>,
    scrollState: ScrollState,
    transformOriginState: State<TransformOrigin>,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // Menu open/close animation.
    @Suppress("DEPRECATION") val transition = updateTransition(expandedState, "DropDownMenu")

    val scale by
    transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                spring(dampingRatio = .6f, stiffness = Spring.StiffnessMedium)
            } else {
                // Expanded to dismissed.
                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
            }
        }
    ) { expanded ->
        if (expanded) ExpandedScaleTarget else ClosedScaleTarget
    }

    val alpha by
    transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                MaterialTheme.motionScheme.fastEffectsSpec()
            } else {
                // Expanded to dismissed.
                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
            }
        }
    ) { expanded ->
        if (expanded) ExpandedAlphaTarget else ClosedAlphaTarget
    }

    val isInspecting = LocalInspectionMode.current

    Surface(
        modifier = modifier.graphicsLayer {
            scaleX =
                if (!isInspecting) scale
                else if (expandedState.targetState) ExpandedScaleTarget else ClosedScaleTarget
            scaleY =
                if (!isInspecting) scale
                else if (expandedState.targetState) ExpandedScaleTarget else ClosedScaleTarget
            this.alpha =
                if (!isInspecting) alpha
                else if (expandedState.targetState) ExpandedAlphaTarget else ClosedAlphaTarget
            transformOrigin = transformOriginState.value
        },
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = ElevationTokens.Level1.dp
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = DropdownMenuVerticalPadding)
                .width(IntrinsicSize.Max)
                .verticalScroll(scrollState), content = content
        )
    }
}


// Size defaults.
internal val MenuVerticalMargin = 48.dp
internal val DropdownMenuVerticalPadding = 8.dp

internal const val InTransitionDuration = 200
internal const val OutTransitionDuration = 100
internal const val ExpandedScaleTarget = 1f
internal const val ClosedScaleTarget = 0.6f
internal const val ExpandedAlphaTarget = 1f
internal const val ClosedAlphaTarget = 0f