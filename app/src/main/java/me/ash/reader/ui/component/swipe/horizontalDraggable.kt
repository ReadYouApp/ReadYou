package me.ash.reader.ui.component.swipe

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.ash.reader.ui.component.swipe.DragEvent.DragCancelled
import me.ash.reader.ui.component.swipe.DragEvent.DragDelta
import me.ash.reader.ui.component.swipe.DragEvent.DragStarted
import me.ash.reader.ui.component.swipe.DragEvent.DragStopped
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.math.sign

/**
 * Workaround for [269627294](https://issuetracker.google.com/issues/269627294).
 *
 * Copy of Compose UI's draggable modifier, but with an additional check to only accept horizontal swipes
 * made within 22.5°. This prevents accidental swipes while scrolling a vertical list.
 */
internal fun Modifier.horizontalDraggable(
  state: DraggableState,
  enabled: Boolean = true,
  startDragImmediately: Boolean = false,
  onDragStarted: suspend CoroutineScope.(startedPosition: Offset) -> Unit = {},
  onDragStopped: suspend CoroutineScope.(velocity: Float) -> Unit = {},
): Modifier = this then DraggableElement(
  state = state,
  enabled = enabled,
  startDragImmediately = { startDragImmediately },
  onDragStarted = onDragStarted,
  onDragStopped = { velocity -> onDragStopped(velocity.x) },
)

internal data class DraggableElement(
  private val state: DraggableState,
  private val enabled: Boolean,
  private val startDragImmediately: () -> Boolean,
  private val onDragStarted: suspend CoroutineScope.(startedPosition: Offset) -> Unit,
  private val onDragStopped: suspend CoroutineScope.(velocity: Velocity) -> Unit,
) : ModifierNodeElement<DraggableNode>() {

  override fun create(): DraggableNode = DraggableNode(
    state,
    enabled,
    startDragImmediately,
    onDragStarted,
    onDragStopped,
  )

  override fun update(node: DraggableNode) {
    node.update(
      state = state,
      enabled = enabled,
      startDragImmediately = startDragImmediately,
      onDragStarted = onDragStarted,
      onDragStopped = onDragStopped,
    )
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "draggable"
    properties["enabled"] = enabled
    properties["startDragImmediately"] = startDragImmediately
    properties["onDragStarted"] = onDragStarted
    properties["onDragStopped"] = onDragStopped
    properties["state"] = state
  }
}

internal class DraggableNode(
  private var state: DraggableState,
  private var enabled: Boolean,
  private var startDragImmediately: () -> Boolean,
  private var onDragStarted: suspend CoroutineScope.(startedPosition: Offset) -> Unit,
  private var onDragStopped: suspend CoroutineScope.(velocity: Velocity) -> Unit,
) : DelegatingNode(), PointerInputModifierNode {

  private val velocityTracker = VelocityTracker()
  private val channel = Channel<DragEvent>(capacity = Channel.UNLIMITED)

  @Suppress("NAME_SHADOWING")
  private val pointerInputNode = SuspendingPointerInputModifierNode {
    if (!enabled) {
      return@SuspendingPointerInputModifierNode
    }

    coroutineScope {
      launch(start = CoroutineStart.UNDISPATCHED) {
        while (isActive) {
          var event = channel.receive()
          if (event !is DragStarted) continue
          onDragStarted.invoke(this, event.startPoint)
          try {
            state.drag(MutatePriority.UserInput) {
              while (event !is DragStopped && event !is DragCancelled) {
                (event as? DragDelta)?.let { dragBy(it.delta.x) }
                event = channel.receive()
              }
            }
            event.let { event ->
              if (event is DragStopped) {
                onDragStopped.invoke(this, event.velocity)
              } else if (event is DragCancelled) {
                onDragStopped.invoke(this, Velocity.Zero)
              }
            }
          } catch (c: CancellationException) {
            onDragStopped.invoke(this, Velocity.Zero)
          }
        }
      }

      awaitEachGesture {
        val awaited = awaitDownAndSlop(
          startDragImmediately = startDragImmediately,
          velocityTracker = velocityTracker,
        )

        if (awaited != null) {
          var isDragSuccessful = false
          try {
            isDragSuccessful = awaitDrag(
              startEvent = awaited.first,
              initialDelta = awaited.second,
              velocityTracker = velocityTracker,
              channel = channel,
              reverseDirection = false,
            )
          } catch (cancellation: CancellationException) {
            isDragSuccessful = false
            if (!isActive) throw cancellation
          } finally {
            val event = if (isDragSuccessful) {
              val velocity = velocityTracker.calculateVelocity()
              velocityTracker.resetTracking()
              DragStopped(velocity)
            } else {
              DragCancelled
            }
            channel.trySend(event)
          }
        }
      }
    }
  }

  init {
    delegate(pointerInputNode)
  }

  override fun onPointerEvent(
    pointerEvent: PointerEvent,
    pass: PointerEventPass,
    bounds: IntSize
  ) {
    pointerInputNode.onPointerEvent(pointerEvent, pass, bounds)
  }

  override fun onCancelPointerInput() {
    pointerInputNode.onCancelPointerInput()
  }

  fun update(
    state: DraggableState,
    enabled: Boolean,
    startDragImmediately: () -> Boolean,
    onDragStarted: suspend CoroutineScope.(startedPosition: Offset) -> Unit,
    onDragStopped: suspend CoroutineScope.(velocity: Velocity) -> Unit,
  ) {
    var resetPointerInputHandling = false
    if (this.state != state) {
      this.state = state
      resetPointerInputHandling = true
    }
    if (this.enabled != enabled) {
      this.enabled = enabled
      resetPointerInputHandling = true
    }
    this.startDragImmediately = startDragImmediately
    this.onDragStarted = onDragStarted
    this.onDragStopped = onDragStopped
    if (resetPointerInputHandling) {
      pointerInputNode.resetPointerInputHandler()
    }
  }
}

private suspend fun AwaitPointerEventScope.awaitDownAndSlop(
  startDragImmediately: () -> Boolean,
  velocityTracker: VelocityTracker,
): Pair<PointerInputChange, Offset>? {
  val initialDown = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
  return if (startDragImmediately()) {
    initialDown.consume()
    velocityTracker.addPointerInputChange(initialDown)
    // since we start immediately we don't wait for slop and the initial delta is 0
    initialDown to Offset.Zero
  } else {
    val down = awaitFirstDown(requireUnconsumed = false)
    velocityTracker.addPointerInputChange(down)
    var initialDelta = Offset.Zero
    val postPointerSlop = { event: PointerInputChange, overSlop: Float ->
      val isHorizontalSwipe = event.positionChange().let {
        abs(it.x) > abs(it.y * 2f)  // Accept swipes made at a max. of 22.5° in either direction.
      }
      if (isHorizontalSwipe) {
        velocityTracker.addPointerInputChange(event)
        event.consume()
        initialDelta = Offset(x = overSlop, 0f)
      } else {
        throw CancellationException()
      }
    }

    val afterSlopResult = awaitHorizontalTouchSlopOrCancellation(
      pointerId = down.id,
      onTouchSlopReached = postPointerSlop
    )

    if (afterSlopResult != null) afterSlopResult to initialDelta else null
  }
}

private suspend fun AwaitPointerEventScope.awaitDrag(
  startEvent: PointerInputChange,
  initialDelta: Offset,
  velocityTracker: VelocityTracker,
  channel: SendChannel<DragEvent>,
  reverseDirection: Boolean,
): Boolean {
  val overSlopOffset = initialDelta
  val xSign = sign(startEvent.position.x)
  val ySign = sign(startEvent.position.y)
  val adjustedStart = startEvent.position -
    Offset(overSlopOffset.x * xSign, overSlopOffset.y * ySign)
  channel.trySend(DragStarted(adjustedStart))

  channel.trySend(DragDelta(if (reverseDirection) initialDelta * -1f else initialDelta))

  return drag(pointerId = startEvent.id) { event ->
    // Velocity tracker takes all events, even UP
    velocityTracker.addPointerInputChange(event)

    // Dispatch only MOVE events
    if (!event.changedToUpIgnoreConsumed()) {
      val delta = event.positionChange()
      event.consume()
      channel.trySend(DragDelta(if (reverseDirection) delta * -1f else delta))
    }
  }
}

private sealed class DragEvent {
  class DragStarted(val startPoint: Offset) : DragEvent()
  class DragStopped(val velocity: Velocity) : DragEvent()
  data object DragCancelled : DragEvent()
  class DragDelta(val delta: Offset) : DragEvent()
}
