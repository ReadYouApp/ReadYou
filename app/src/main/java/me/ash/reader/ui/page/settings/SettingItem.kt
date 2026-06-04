/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 * @modifier Ashinch
 */

package me.ash.reader.ui.page.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.ui.theme.palette.LocalTonalPalettes
import me.ash.reader.ui.theme.palette.onDark

val LocalInteractionSources = compositionLocalOf<MutableInteractionSource?> { null }

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String,
    desc: String? = null,
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    separatedActions: Boolean = false,
    onClick: () -> Unit,
    action: (@Composable () -> Unit)? = null,
) {
    val tonalPalettes = LocalTonalPalettes.current
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .clickable(enabled = enabled, interactionSource = interactionSource) { onClick() }
            .alpha(if (enabled) 1f else 0.5f),
        color = Color.Unspecified
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 16.dp, 16.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    modifier = Modifier.padding(end = 24.dp),
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                iconPainter?.let {
                    Icon(
                        modifier = Modifier
                            .padding(end = 24.dp)
                            .size(24.dp),
                        painter = it,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    maxLines = if (desc == null) 2 else 1,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
                )
                desc?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            action?.let {
                if (separatedActions) {
                    VerticalDivider(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(1.dp, 32.dp),
                        color = tonalPalettes neutralVariant 80 onDark (tonalPalettes neutralVariant 30)
                    )
                }
                CompositionLocalProvider(LocalInteractionSources provides if (separatedActions) null else interactionSource) {
                    Box(Modifier.padding(start = 16.dp)) {
                        it()
                    }
                }
            }
        }
    }
}
