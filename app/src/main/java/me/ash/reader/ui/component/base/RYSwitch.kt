/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 * @modifier Ashinch
 */

package me.ash.reader.ui.component.base

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.ash.reader.ui.page.settings.LocalInteractionSources

@Composable
fun RYSwitch(
    modifier: Modifier = Modifier,
    activated: Boolean,
    enable: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Switch(
        modifier = modifier,
        checked = activated,
        enabled = enable,
        onCheckedChange = { onClick?.invoke() },
        thumbContent = { SwitchThumbIcon(checked = activated) },
        interactionSource = LocalInteractionSources.current
    )
}


@Composable
fun SwitchThumbIcon(checked: Boolean, modifier: Modifier = Modifier) {
    val sizeModifier = modifier.size(SwitchDefaults.IconSize)
    if (checked) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            modifier = sizeModifier
        )
    }
}