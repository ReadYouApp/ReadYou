package me.ash.reader.ui.component.scrollbar

/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.compose.foundation.ScrollIndicatorState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo

/**
 * A factory for creating a scroll indicator, such as a scrollbar, that can be attached to a
 * scrollable component.
 *
 * Implementations of this interface define the appearance and behavior of the scroll indicator. For
 * example, a platform-specific scrollbar or a custom designed indicator can be created by
 * implementing this interface.
 *
 * @see Modifier.scrollIndicator
 */
@Stable
interface ScrollIndicatorFactory {
    /**
     * Creates a [DelegatableNode] that draws the scroll indicator and handles its interactions.
     *
     * This node is attached to the hierarchy via the [Modifier.scrollIndicator] modifier.
     *
     * @param state The [ScrollIndicatorState] for the scrollable component.
     * @param orientation The scrolling orientation of the container.
     * @return A new instance of [DelegatableNode] that represents the scroll indicator.
     */
    fun createNode(state: ScrollIndicatorState, orientation: Orientation): DelegatableNode

    /**
     * Require hashCode() to be implemented. Using a data class is sufficient. Singletons and
     * instances with no properties may implement this function by returning an arbitrary constant.
     */
    override fun hashCode(): Int

    /**
     * Require equals() to be implemented. Using a data class is sufficient. Singletons may
     * implement this function with referential equality (`this === other`). Instances with no
     * properties may implement this function by checking the type of the other object.
     */
    override fun equals(other: Any?): Boolean
}

/**
 * A modifier that draws and handles interactions for a scroll indicator (e.g., a scrollbar) defined
 * by the provided [factory].
 *
 * The [ScrollIndicatorFactory] is responsible for creating the UI and behavior of the indicator,
 * while the [state] provides the necessary data, like scroll offset and content size, to correctly
 * represent the scroll position.
 *
 * @sample androidx.compose.foundation.samples.VisualScrollbarSample
 * @param factory The [ScrollIndicatorFactory] that creates the scroll indicator.
 * @param state The [ScrollIndicatorState] for the scrollable component.
 * @param orientation The scrolling orientation of the container. The indicator will be drawn and
 *   interact based on this orientation.
 */
fun Modifier.scrollIndicator(
    factory: ScrollIndicatorFactory,
    state: ScrollIndicatorState,
    orientation: Orientation,
): Modifier {
    return this.then(ScrollIndicatorModifierElement(factory, state, orientation))
}

private class ScrollIndicatorModifierElement(
    private val factory: ScrollIndicatorFactory,
    private val state: ScrollIndicatorState,
    private val orientation: Orientation,
) : ModifierNodeElement<ScrollIndicatorDelegatingNode>() {
    override fun create(): ScrollIndicatorDelegatingNode =
        ScrollIndicatorDelegatingNode(factory, state, orientation)

    override fun update(node: ScrollIndicatorDelegatingNode) {
        node.update(factory, state, orientation)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScrollIndicatorModifierElement) return false
        if (factory != other.factory) return false
        if (state != other.state) return false
        if (orientation != other.orientation) return false
        return true
    }

    override fun hashCode(): Int {
        var result = factory.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + orientation.hashCode()
        return result
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "scrollIndicator"
        properties["factory"] = factory
        properties["state"] = state
        properties["orientation"] = orientation
    }
}

/**
 * A [DelegatingNode] that manages the lifecycle of the node created by [ScrollIndicatorFactory].
 */
private class ScrollIndicatorDelegatingNode(
    private var factory: ScrollIndicatorFactory,
    private var state: ScrollIndicatorState,
    private var orientation: Orientation,
) : DelegatingNode() {
    private var scrollIndicatorNode = factory.createNode(state, orientation)

    override fun onAttach() {
        delegate(scrollIndicatorNode)
    }

    fun update(
        factory: ScrollIndicatorFactory,
        state: ScrollIndicatorState,
        orientation: Orientation,
    ) {
        if (this.factory != factory || this.state != state || this.orientation != orientation) {
            undelegate(this.scrollIndicatorNode)
            this.factory = factory
            this.state = state
            this.orientation = orientation
            this.scrollIndicatorNode = factory.createNode(state, orientation)
            delegate(scrollIndicatorNode)
        }
    }

    override fun onDetach() {
        undelegate(scrollIndicatorNode)
    }
}
