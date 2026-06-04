package me.ash.reader.ui.component.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RYExtensibleVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(animationSpec = MaterialTheme.motionScheme.slowEffectsSpec()) + expandVertically(
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
            expandFrom = Alignment.Top
        ),
        exit = fadeOut(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()) + shrinkVertically(
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
            shrinkTowards = Alignment.Top
        ),
        content = content,
    )
}
