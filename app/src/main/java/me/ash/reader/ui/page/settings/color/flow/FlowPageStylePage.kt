package me.ash.reader.ui.page.settings.color.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.*
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun FlowPageStylePage(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val filterBarStyle = LocalFlowFilterBarStyle.current
    val filterBarPadding = LocalFlowFilterBarPadding.current
    val filterBarTonalElevation = LocalFlowFilterBarTonalElevation.current
    val topBarTonalElevation = LocalFlowTopBarTonalElevation.current
    val articleListFeedIcon = LocalFlowArticleListFeedIcon.current
    val articleListFeedName = LocalFlowArticleListFeedName.current
    val articleListImage = LocalFlowArticleListImage.current
    val articleListDesc = LocalFlowArticleListDesc.current
    val articleListTime = LocalFlowArticleListTime.current
    val articleListStickyDate = LocalFlowArticleListDateStickyHeader.current
    val articleListTonalElevation = LocalFlowArticleListTonalElevation.current
    val articleListReadIndicator = LocalFlowArticleListReadIndicator.current
    val sortUnreadArticles = LocalSortUnreadArticles.current

    val settings = LocalSettings.current
    val pullToSwitchFeed = settings.pullToSwitchFeed

    val scope = rememberCoroutineScope()

    var filterBarStyleDialogVisible by remember { mutableStateOf(false) }
    var filterBarPaddingDialogVisible by remember { mutableStateOf(false) }
    var filterBarTonalElevationDialogVisible by remember { mutableStateOf(false) }
    var topBarTonalElevationDialogVisible by remember { mutableStateOf(false) }
    var articleListTonalElevationDialogVisible by remember { mutableStateOf(false) }
    var articleListReadIndicatorDialogVisible by remember { mutableStateOf(false) }
    var showArticleListDescDialog by remember { mutableStateOf(false) }
    var showPullToLoadDialog by remember { mutableStateOf(false) }

    var showSortUnreadArticlesDialog by remember { mutableStateOf(false) }

    var filterBarPaddingValue: Int? by remember { mutableStateOf(filterBarPadding) }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.flow_page), desc = "")
                }

                // Preview
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                MaterialTheme.colorScheme.inverseOnSurface
                                        onLight MaterialTheme.colorScheme.surface.copy(0.7f)
                            )
                            .clickable { },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FlowPagePreview(
                            topBarTonalElevation = topBarTonalElevation,
                            articleListTonalElevation = articleListTonalElevation,
                            filterBarStyle = filterBarStyle.value,
                            filterBarFilled = true,
                            filterBarPadding = filterBarPadding.dp,
                            filterBarTonalElevation = filterBarTonalElevation.value.dp,
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Top Bar
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.top_bar)
                    )
                    SettingItem(
                        title = stringResource(R.string.mark_as_read_button_position),
                        desc = stringResource(R.string.top),
                        enabled = false,
                        onClick = {},
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.tonal_elevation),
                        desc = "${topBarTonalElevation.value}dp",
                        onClick = {
                            topBarTonalElevationDialogVisible = true
                        },
                    ) {}
                    Tips(text = stringResource(R.string.tips_top_bar_tonal_elevation))
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Article List
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.article_list)
                    )
                    SettingItem(
                        title = stringResource(R.string.feed_favicons),
                        onClick = {
                            (!articleListFeedIcon).put(context, scope)
                        },
                    ) {
                        RYSwitch(activated = articleListFeedIcon.value) {
                            (!articleListFeedIcon).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.feed_names),
                        onClick = {
                            (!articleListFeedName).put(context, scope)
                        },
                    ) {
                        RYSwitch(activated = articleListFeedName.value) {
                            (!articleListFeedName).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.article_images),
                        onClick = {
                            (!articleListImage).put(context, scope)
                        },
                    ) {
                        RYSwitch(activated = articleListImage.value) {
                            (!articleListImage).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.article_desc),
                        desc = articleListDesc.description(),
                        onClick = {
                            showArticleListDescDialog = true
                        },
                    )
                    SettingItem(
                        title = stringResource(R.string.article_date),
                        onClick = {
                            (!articleListTime).put(context, scope)
                        },
                    ) {
                        RYSwitch(activated = articleListTime.value) {
                            (!articleListTime).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.article_date_sticky_header),
                        onClick = {
                            (!articleListStickyDate).put(context, scope)
                        },
                    ) {
                        RYSwitch(activated = articleListStickyDate.value) {
                            (!articleListStickyDate).put(context, scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.grey_out_articles),
                        desc = articleListReadIndicator.description,
                        onClick = {
                            articleListReadIndicatorDialogVisible = true
                        }
                    )
                    SettingItem(
                        title = stringResource(R.string.sort_unread_articles),
                        onClick = {
                            showSortUnreadArticlesDialog = true
                        },
                        desc = sortUnreadArticles.description()
                    ) {
                    }

                    SettingItem(
                        title = stringResource(R.string.pull_from_bottom),
                        desc = pullToSwitchFeed.description(),
                        onClick = {
                            showPullToLoadDialog = true
                        },
                    )

                    SettingItem(
                        title = stringResource(R.string.tonal_elevation),
                        desc = "${articleListTonalElevation.value}dp",
                        onClick = {
                            articleListTonalElevationDialogVisible = true
                        },
                    ) {}
                    Tips(text = stringResource(R.string.tips_article_list_tonal_elevation))
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Filter Bar
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.filter_bar),
                    )
                    SettingItem(
                        title = stringResource(R.string.style),
                        desc = filterBarStyle.toDesc(context),
                        onClick = {
                            filterBarStyleDialogVisible = true
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.horizontal_padding),
                        desc = "${filterBarPadding}dp",
                        onClick = {
                            filterBarPaddingValue = filterBarPadding
                            filterBarPaddingDialogVisible = true
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.tonal_elevation),
                        desc = "${filterBarTonalElevation.value}dp",
                        onClick = {
                            filterBarTonalElevationDialogVisible = true
                        },
                    ) {}
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    RadioDialog(
        visible = filterBarStyleDialogVisible,
        title = stringResource(R.string.style),
        options = FlowFilterBarStylePreference.values.map {
            RadioDialogOption(
                text = it.toDesc(context),
                selected = it == filterBarStyle,
            ) {
                it.put(context, scope)
            }
        }
    ) {
        filterBarStyleDialogVisible = false
    }

    TextFieldDialog(
        visible = filterBarPaddingDialogVisible,
        title = stringResource(R.string.horizontal_padding),
        value = (filterBarPaddingValue ?: "").toString(),
        placeholder = stringResource(R.string.value),
        onValueChange = {
            filterBarPaddingValue = it.filter { it.isDigit() }.toIntOrNull()
        },
        onDismissRequest = {
            filterBarPaddingDialogVisible = false
        },
        onConfirm = {
            FlowFilterBarPaddingPreference.put(context, scope, filterBarPaddingValue ?: 0)
            filterBarPaddingDialogVisible = false
        }
    )

    RadioDialog(
        visible = filterBarTonalElevationDialogVisible,
        title = stringResource(R.string.tonal_elevation),
        options = FlowFilterBarTonalElevationPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(context),
                selected = it == filterBarTonalElevation,
            ) {
                it.put(context, scope)
            }
        }
    ) {
        filterBarTonalElevationDialogVisible = false
    }

    RadioDialog(
        visible = topBarTonalElevationDialogVisible,
        title = stringResource(R.string.tonal_elevation),
        options = FlowTopBarTonalElevationPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(context),
                selected = it == topBarTonalElevation,
            ) {
                it.put(context, scope)
            }
        }
    ) {
        topBarTonalElevationDialogVisible = false
    }

    RadioDialog(
        visible = articleListTonalElevationDialogVisible,
        title = stringResource(R.string.tonal_elevation),
        options = FlowArticleListTonalElevationPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(context),
                selected = it == articleListTonalElevation,
            ) {
                it.put(context, scope)
            }
        }
    ) {
        articleListTonalElevationDialogVisible = false
    }

    RadioDialog(
        visible = showArticleListDescDialog,
        title = stringResource(R.string.article_desc),
        options = FlowArticleListDescPreference.values.map {
            RadioDialogOption(
                text = it.description(),
                selected = it == articleListDesc,
            ) {
                it.put(context, scope)
            }
        },
        onDismissRequest = {
            showArticleListDescDialog = false
        }
    )

    RadioDialog(
        visible = articleListReadIndicatorDialogVisible,
        title = stringResource(id = R.string.grey_out_articles),
        options = FlowArticleReadIndicatorPreference.values.map {
            RadioDialogOption(
                text = it.description,
                selected = it == articleListReadIndicator
            ) {
                it.put(context, scope)
            }
        }
    ) {
        articleListReadIndicatorDialogVisible = false
    }

    RadioDialog(
        visible = showSortUnreadArticlesDialog,
        title = stringResource(R.string.sort_unread_articles),
        options = SortUnreadArticlesPreference.values.map {
            RadioDialogOption(
                text = it.description(),
                selected = it == sortUnreadArticles,
            ) {
                it.put(context, scope)
            }
        },
        onDismissRequest = {
            showSortUnreadArticlesDialog = false
        }
    )

    RadioDialog(
        visible = showPullToLoadDialog,
        title = stringResource(R.string.pull_from_bottom),
        options = PullToLoadNextFeedPreference.values.map {
            RadioDialogOption(
                text = it.description(),
                selected = it == pullToSwitchFeed,
            ) {
                it.put(context, scope)
            }
        },
        onDismissRequest = {
            showPullToLoadDialog = false
        }
    )
}
