package me.ash.reader.ui.component.base

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackIconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String?,
    tint: Color = LocalContentColor.current,
    enabled: Boolean = true,
    showBadge: Boolean = false,
    isHaptic: Boolean? = true,
    isSound: Boolean? = true,
    onClick: () -> Unit = {},
) {
    val view = LocalView.current

    BadgedBox(
        badge = {
            if (showBadge) {
                Badge(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                )
            }
        }
    ) {
        IconButton(
            enabled = enabled,
            onClick = {
                if (isHaptic == true) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                if (isSound == true) view.playSoundEffect(SoundEffectConstants.CLICK)
                onClick()
            },
        ) {
            Icon(
                modifier = modifier,
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = tint,
            )
        }
    }
}
