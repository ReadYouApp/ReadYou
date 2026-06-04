package me.ash.reader.ui.page.home.flow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp

private val CenterBottom = TransformOrigin(.5f, 1f)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScrollToLastReadFab(visible: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            transformOrigin = CenterBottom,
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
        ),
        exit = scaleOut(
            transformOrigin = CenterBottom,
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
        ) + fadeOut(
            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
        ),
        modifier = modifier.padding(bottom = 12.dp)
    ) {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.loweredElevation(),
            containerColor = MaterialTheme.colorScheme.primaryFixedDim,
            contentColor = MaterialTheme.colorScheme.onPrimaryFixedVariant
        ) {
            Icon(Icons.Rounded.ArrowDownward, null)
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScrollToTopFab(visible: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            transformOrigin = CenterBottom,
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
        ),
        exit = scaleOut(
            transformOrigin = CenterBottom,
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
        ) + fadeOut(
            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
        ),
        modifier = modifier.padding(bottom = 12.dp)
    ) {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.loweredElevation(),
            containerColor = MaterialTheme.colorScheme.primaryFixedDim,
            contentColor = MaterialTheme.colorScheme.onPrimaryFixedVariant
        ) {
            Icon(Icons.Rounded.ArrowUpward, null)
        }
    }
}