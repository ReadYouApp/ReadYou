package me.ash.reader.ui.page.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalNewVersionNumber
import me.ash.reader.infrastructure.preference.LocalSkipVersionNumber
import me.ash.reader.infrastructure.preference.toDisplayName
import me.ash.reader.ui.component.base.Banner
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.getCurrentVersion
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.settings.tips.UpdateDialog
import me.ash.reader.ui.page.settings.tips.UpdateViewModel
import me.ash.reader.ui.theme.palette.onLight
import java.util.Locale

@Composable
fun SettingsPage(
    navController: NavHostController,
    updateViewModel: UpdateViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val newVersion = LocalNewVersionNumber.current
    val skipVersion = LocalSkipVersionNumber.current
    val currentVersion by remember { mutableStateOf(context.getCurrentVersion()) }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface
            ) {
                navController.popBackStack()
            }
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.settings), desc = "")
                }
                item {
                    Box {
                        if (newVersion.whetherNeedUpdate(currentVersion, skipVersion)) {
                            Banner(
                                modifier = Modifier.zIndex(1f),
                                title = stringResource(R.string.get_new_updates),
                                desc = stringResource(
                                    R.string.get_new_updates_desc,
                                    newVersion.toString(),
                                ),
                                icon = Icons.Outlined.Lightbulb,
                                action = {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.close),
                                    )
                                },
                            ) {
                                updateViewModel.showDialog()
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        // Banner(
                        //     title = stringResource(R.string.in_coding),
                        //     desc = stringResource(R.string.coming_soon),
                        //     icon = Icons.Outlined.Lightbulb,
                        // )
                    }
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.accounts),
                        desc = stringResource(R.string.accounts_desc),
                        icon = Icons.Outlined.AccountCircle,
                    ) {
                        navController.navigate(RouteName.ACCOUNTS) {
                            launchSingleTop = true
                        }
                    }
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.color_and_style),
                        desc = stringResource(R.string.color_and_style_desc),
                        icon = Icons.Outlined.Palette,
                    ) {
                        navController.navigate(RouteName.COLOR_AND_STYLE) {
                            launchSingleTop = true
                        }
                    }
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.interaction),
                        desc = stringResource(R.string.interaction_desc),
                        icon = Icons.Outlined.TouchApp,
                    ) {
                        navController.navigate(RouteName.INTERACTION) {
                            launchSingleTop = true
                        }
                    }
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.filters),
                        desc = stringResource(R.string.filters_desc),
                        icon = Icons.Outlined.FilterList,
                    ) {
                        navController.navigate(RouteName.FILTERS) {
                            launchSingleTop = true
                        }
                    }
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.languages),
                        desc = Locale.getDefault().toDisplayName(),
                        icon = Icons.Outlined.Language,
                    ) {
                        navController.navigate(RouteName.LANGUAGES) {
                            launchSingleTop = true
                        }
                    }
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.troubleshooting),
                        desc = stringResource(R.string.troubleshooting_desc),
                        icon = Icons.Outlined.BugReport,
                    ) {
                        navController.navigate(RouteName.TROUBLESHOOTING) {
                            launchSingleTop = true
                        }
                    }
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.tips_and_support),
                        desc = stringResource(R.string.tips_and_support_desc),
                        icon = Icons.Outlined.TipsAndUpdates,
                    ) {
                        navController.navigate(RouteName.TIPS_AND_SUPPORT) {
                            launchSingleTop = true
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    UpdateDialog()
}
