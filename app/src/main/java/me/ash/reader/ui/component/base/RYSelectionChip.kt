package me.ash.reader.ui.component.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RYSelectionChip(
    content: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = CircleShape,
    border: BorderStroke? = null,
    selectedIcon: @Composable (() -> Unit)? = {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
        )
    },
    onClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    androidx.compose.material3.FilterChip(
        modifier = modifier.defaultMinSize(minHeight = 36.dp),
        colors =
            FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledSelectedContainerColor =
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                selectedContainerColor = MaterialTheme.colorScheme.primaryFixed,
                labelColor = MaterialTheme.colorScheme.onSurface,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryFixed,
                iconColor = MaterialTheme.colorScheme.onSurface,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryFixed,
            ),
        selected = selected,
        interactionSource = interactionSource,
        border = border,
        enabled = enabled,
        shape = shape,
        onClick = {
            focusManager.clearFocus()
            onClick()
        },
        label = {
            Text(
                content,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        },
        leadingIcon = {
            if (selectedIcon != null) {
                val expandSpec = MaterialTheme.motionScheme.defaultEffectsSpec<IntSize>()
                val fadeInSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()
                val shrinkSpec = MaterialTheme.motionScheme.defaultEffectsSpec<IntSize>()
                val fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
                Row {
                    AnimatedVisibility(
                        selected,
                        enter =
                            expandHorizontally(
                                animationSpec = expandSpec,
                                expandFrom = Alignment.Start,
                            ) + fadeIn(animationSpec = fadeInSpec),
                        exit =
                            shrinkHorizontally(
                                animationSpec = shrinkSpec,
                                shrinkTowards = Alignment.Start,
                            ) + fadeOut(animationSpec = fadeOutSpec),
                    ) {
                        selectedIcon()
                    }
                }
            }
        },
    )
}

@Preview
@Composable
private fun RYSelectionChipPreview() {
    Column {
        var selected by remember { mutableStateOf(false) }
        RYSelectionChip(content = "Test", selected = selected, onClick = { selected = !selected })
    }
}
