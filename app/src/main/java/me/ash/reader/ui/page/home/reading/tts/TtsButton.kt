package me.ash.reader.ui.page.home.reading.tts

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.ash.reader.R
import me.ash.reader.infrastructure.android.TextToSpeechManager
import me.ash.reader.ui.motion.Direction
import me.ash.reader.ui.motion.sharedYAxisTransitionSlow

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TtsButton(
    modifier: Modifier = Modifier,
    onClick: (TextToSpeechManager.State) -> Unit,
    state: TextToSpeechManager.State
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val motionScheme = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = state,
        contentKey = { it is TextToSpeechManager.State.Reading },
        transitionSpec = {
            sharedYAxisTransitionSlow(
                direction = if (targetState is TextToSpeechManager.State.Reading) Direction.Forward else Direction.Backward,
                motionScheme = motionScheme
            )
        }) { state ->
        when (state) {
            is TextToSpeechManager.State.Reading -> {
                Box(
                    modifier = modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onClick(state)
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    TtsProgressIndicator(
                        { state.progress },
                        modifier = Modifier
                    )
                    Icon(
                        Icons.Rounded.Stop,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            else -> {
                Box(
                    modifier = modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .combinedClickable(onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onClick(state)
                        }, onLongClick = {
                            try {
                                val intent = Intent().apply {
                                    action = "com.android.settings.TTS_SETTINGS"
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    "TTS settings screen not found.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Headphones,
                        stringResource(R.string.read_aloud),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TtsProgressIndicator(progress: (() -> Float), modifier: Modifier = Modifier) {

    val animatedProgress by animateFloatAsState(
        targetValue = progress(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    CircularProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier.size(24.dp),
        gapSize = 0.dp,
        strokeCap = StrokeCap.Butt,
        trackColor = MaterialTheme.colorScheme.outlineVariant,
        strokeWidth = 3.dp
    )
}

@Preview
@Composable
private fun Preview() {
    Column {
        TtsProgressIndicator({ .5f })
//        TtsButton(onClick = {}, state = TextToSpeechManager.State.Idle)
//        TtsButton(onClick = {}, state = TextToSpeechManager.State.Reading(2, 4))
        var state: TextToSpeechManager.State by remember { mutableStateOf(TextToSpeechManager.State.Idle) }
        TtsButton(onClick = {
            state = if (state == TextToSpeechManager.State.Idle) {
                TextToSpeechManager.State.Reading(2, 4)
            } else {
                TextToSpeechManager.State.Idle
            }
        }, state = state)
    }

}