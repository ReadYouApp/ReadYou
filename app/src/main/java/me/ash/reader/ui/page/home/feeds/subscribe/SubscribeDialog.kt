package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.ui.component.FeedIcon
import me.ash.reader.ui.component.RenameDialog
import me.ash.reader.ui.component.base.ClipboardTextField
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.ext.MimeType
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.roundClick
import me.ash.reader.ui.page.home.feeds.FeedOptionView

@OptIn(
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun SubscribeDialog(
    subscribeViewModel: SubscribeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val subscribeUiState = subscribeViewModel.subscribeUiState.collectAsStateValue()
    val subscribeState = subscribeViewModel.subscribeState.collectAsStateValue()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let { uri ->
            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                subscribeViewModel.importFromInputStream(inputStream)
            }
        }
    }

    if (subscribeState is SubscribeState.Visible) {

        DisposableEffect(Unit) {
            onDispose {
                subscribeViewModel.cancelSearch()
            }
        }

        AlertDialog(
            modifier = Modifier.padding(horizontal = 44.dp),
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false
            ),
            onDismissRequest = {
                focusManager.clearFocus()
                subscribeViewModel.hideDrawer()
            },
            icon = {
                val iconUrl = when (subscribeState) {
                    is SubscribeState.Configure -> subscribeState.searchedFeed.icon.url
                    else -> null
                }
                FeedIcon(
                    feedName = null,
                    iconUrl = iconUrl,
                    placeholderIcon = Icons.Rounded.RssFeed,
                )
            },
            title = {
                Text(
                    modifier = Modifier.roundClick {
                        if (subscribeState is SubscribeState.Configure) {
                            subscribeViewModel.showRenameDialog()
                        }
                    },
                    text = when (subscribeState) {
                        is SubscribeState.Configure -> subscribeState.searchedFeed.title
                        is SubscribeState.Fetching -> stringResource(R.string.searching)
                        is SubscribeState.Idle -> stringResource(R.string.subscribe)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            text = {
                AnimatedContent(
                    targetState = subscribeState,
                    transitionSpec = {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()) using null
                    },
                    contentKey = { it is SubscribeState.Configure }
                ) { state ->
                    when (state) {
                        is SubscribeState.Input -> {
                            val errorText = when (state) {
                                is SubscribeState.Fetching -> ""
                                is SubscribeState.Idle -> state.errorMessage ?: ""
                            }

                            ClipboardTextField(
                                state = state.linkState,
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = state is SubscribeState.Fetching,
                                placeholder = stringResource(R.string.feed_or_site_url),
                                errorText = errorText,
                                imeAction = ImeAction.Search,
                                onConfirm = {
                                    subscribeViewModel.searchFeed()
                                },
                            )
                        }

                        is SubscribeState.Configure -> {
                            FeedOptionView(
                                link = state.feedLink,
                                groups = state.groups,
                                selectedAllowNotificationPreset = state.notification,
                                selectedParseFullContentPreset = state.fullContent,
                                selectedOpenInBrowserPreset = state.browser,
                                selectedGroupId = state.selectedGroupId,
                                allowNotificationPresetOnClick = {
                                    subscribeViewModel.toggleAllowNotificationPreset()
                                },
                                parseFullContentPresetOnClick = {
                                    subscribeViewModel.toggleParseFullContentPreset()
                                },
                                openInBrowserPresetOnClick = {
                                    subscribeViewModel.toggleOpenInBrowserPreset()
                                },
                                onGroupClick = {
                                    subscribeViewModel.selectedGroup(it)
                                },
                                onAddNewGroup = {
                                    subscribeViewModel.showNewGroupDialog()
                                },
                            )
                        }

                        SubscribeState.Hidden -> {}
                    }
                }
            },
            confirmButton = {
                when (subscribeState) {
                    is SubscribeState.Configure -> {
                        TextButton(
                            onClick = {
                                focusManager.clearFocus()
                                subscribeViewModel.subscribe()
                            }
                        ) {
                            Text(stringResource(R.string.subscribe))
                        }
                    }

                    is SubscribeState.Input -> {
                        val enabled =
                            subscribeState is SubscribeState.Idle && subscribeState.linkState.text.isNotBlank()
                        TextButton(
                            enabled = enabled,
                            onClick = {
                                focusManager.clearFocus()
                                subscribeViewModel.searchFeed()
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.search),
                            )
                        }
                    }
                }
            },
            dismissButton = {
                if (subscribeState is SubscribeState.Idle && subscribeState.importFromOpmlEnabled) {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            launcher.launch(arrayOf(MimeType.ANY))
                            subscribeViewModel.hideDrawer()
                        }
                    ) {
                        Text(text = stringResource(R.string.import_from_opml))
                    }
                } else {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            subscribeViewModel.hideDrawer()
                        }
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            },
        )

        RenameDialog(
            visible = subscribeUiState.renameDialogVisible,
            value = subscribeUiState.newName,
            onValueChange = {
                subscribeViewModel.inputNewName(it)
            },
            onDismissRequest = {
                subscribeViewModel.hideRenameDialog()
            },
            onConfirm = {
                subscribeViewModel.renameFeed()
                subscribeViewModel.hideRenameDialog()
            }
        )

        TextFieldDialog(
            visible = subscribeUiState.newGroupDialogVisible,
            title = stringResource(R.string.create_new_group),
            icon = Icons.Outlined.CreateNewFolder,
            value = subscribeUiState.newGroupContent,
            placeholder = stringResource(R.string.name),
            onValueChange = {
                subscribeViewModel.inputNewGroup(it)
            },
            onDismissRequest = {
                subscribeViewModel.hideNewGroupDialog()
            },
            onConfirm = {
                subscribeViewModel.addNewGroup()
            }
        )
    }
}
