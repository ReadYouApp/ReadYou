package me.ash.reader.ui.page.home.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MenuOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalReadingPageTonalElevation
import me.ash.reader.infrastructure.preference.LocalSharedContent
import me.ash.reader.infrastructure.preference.ReadingPageTonalElevationPreference
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.page.adaptive.NavigationAction

private val sizeSpec = spring<IntSize>(stiffness = 700f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    isShow: Boolean,
    isScrolled: Boolean = false,
    title: String? = "",
    link: String? = "",
    navigationAction: NavigationAction,
    onClick: (() -> Unit)? = null,
    onNavButtonClick: (NavigationAction) -> Unit = {},
    onNavigateToStylePage: () -> Unit,
) {
    val context = LocalContext.current
    val sharedContent = LocalSharedContent.current
    val isOutlined =
        LocalReadingPageTonalElevation.current == ReadingPageTonalElevationPreference.Outlined

    val containerColor by
        animateColorAsState(
            with(MaterialTheme.colorScheme) {
                if (isOutlined || !isScrolled) surface else surfaceContainer
            },
            label = "",
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        )

    Box(modifier = Modifier.fillMaxSize().zIndex(1f), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.drawBehind { drawRect(containerColor) }) {
            Spacer(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            )
            AnimatedVisibility(
                visible = isShow,
                enter = expandVertically(expandFrom = Alignment.Bottom, animationSpec = sizeSpec),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom, animationSpec = sizeSpec),
            ) {
                TopAppBar(
                    title = {},
                    modifier =
                        if (onClick == null) Modifier
                        else
                            Modifier.clickable(
                                onClick = onClick,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ),
                    windowInsets = WindowInsets(0.dp),
                    navigationIcon = {
                        val imageVector =
                            when (navigationAction) {
                                NavigationAction.Close -> Icons.Rounded.Close
                                NavigationAction.HideList -> Icons.AutoMirrored.Rounded.MenuOpen
                                NavigationAction.ExpandList -> Icons.Rounded.Menu
                            }
                        val contentDescription =
                            when (navigationAction) {
                                NavigationAction.Close -> stringResource(R.string.close)
                                NavigationAction.HideList -> "Hide list"
                                NavigationAction.ExpandList -> "Expand list"
                            }
                        FeedbackIconButton(
                            imageVector = imageVector,
                            contentDescription = contentDescription,
                            tint = MaterialTheme.colorScheme.onSurface,
                        ) {
                            onNavButtonClick(navigationAction)
                        }
                    },
                    actions = {
                        FeedbackIconButton(
                            modifier = Modifier.size(22.dp),
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = stringResource(R.string.style),
                            tint = MaterialTheme.colorScheme.onSurface,
                        ) {
                            onNavigateToStylePage()
                        }
                        FeedbackIconButton(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.Outlined.Share,
                            contentDescription = stringResource(R.string.share),
                            tint = MaterialTheme.colorScheme.onSurface,
                        ) {
                            sharedContent.share(context, title, link)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            }
            if (isOutlined && isScrolled) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    thickness = 0.5f.dp,
                )
            }
        }
    }
}
