/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 * @modifier Ashinch
 */

package me.ash.reader.ui.component.base

import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Banner(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryFixed,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Banner(
        modifier = modifier,
        title = title,
        desc = desc,
        backgroundColor = backgroundColor,
        contentColor = MaterialTheme.colorScheme.contentColorFor(backgroundColor),
        icon = icon?.let {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier,
                    tint = MaterialTheme.colorScheme.contentColorFor(backgroundColor),
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
        },
        action = action,
        onClick = onClick
    )
}


@Composable
fun Banner(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryFixed,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(backgroundColor),
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val view = LocalView.current

    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(32.dp),
        contentColor = contentColor,
        onClick = {
            view.playSoundEffect(SoundEffectConstants.CLICK)
            onClick()
        }
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 88.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.invoke()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    maxLines = if (desc == null) 2 else 1,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    overflow = TextOverflow.Ellipsis,
                )
                desc?.let {
                    Text(
//                        modifier = Modifier.animateContentSize(tween()),
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = .7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            action?.let {
                Box(Modifier.padding(start = 16.dp)) {
                    it()
                }
            }
        }
    }
}