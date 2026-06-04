package me.ash.reader.ui.component.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.theme.palette.onDark

@OptIn(ExperimentalMaterial3Api::class)
@Deprecated("Use m3 Scaffold instead")
@Composable
fun RYScaffold(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    topBarTonalElevation: Dp = 0.dp,
    containerTonalElevation: Dp = 0.dp,
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier =
            modifier.background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(
                    topBarTonalElevation,
                    color = containerColor,
                )
            ),
        containerColor =
            MaterialTheme.colorScheme.surfaceColorAtElevation(
                containerTonalElevation,
                color = containerColor,
            ) onDark MaterialTheme.colorScheme.surface,
        topBar = {
            if (topBar != null) topBar()
            else if (navigationIcon != null || actions != null) {
                TopAppBar(
                    title = {},
                    navigationIcon = { navigationIcon?.invoke() },
                    actions = { actions?.invoke(this) },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor =
                                MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    topBarTonalElevation
                                )
                        ),
                )
            }
        },
        content = {
            val layoutDirection = LocalLayoutDirection.current
            Column(
                modifier =
                    Modifier.padding(
                        start = it.calculateStartPadding(layoutDirection),
                        end = it.calculateEndPadding(layoutDirection),
                    )
            ) {
                Spacer(modifier = Modifier.height(it.calculateTopPadding()))
                content()
            }
        },
        bottomBar = { bottomBar?.invoke() },
        floatingActionButton = { floatingActionButton?.invoke() },
        floatingActionButtonPosition = floatingActionButtonPosition,
    )
}
