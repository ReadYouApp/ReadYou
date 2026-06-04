package me.ash.reader.ui.page.home.feeds.drawer.feed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.showToast

@Composable
fun DeleteFeedDialog(
    feedName: String,
    feedOptionViewModel: FeedOptionViewModel = hiltViewModel(),
    onConfirm: () -> Unit,
) {
    val context = LocalContext.current
    val feedOptionUiState = feedOptionViewModel.feedOptionUiState.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val toastString = stringResource(R.string.delete_toast, feedName)

    RYDialog(
        visible = feedOptionUiState.deleteDialogVisible,
        onDismissRequest = {
            feedOptionViewModel.hideDeleteDialog()
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteForever,
                contentDescription = stringResource(R.string.unsubscribe),
            )
        },
        title = {
            Text(text = stringResource(R.string.unsubscribe))
        },
        text = {
            Text(text = stringResource(R.string.unsubscribe_tips, feedName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    feedOptionViewModel.delete {
                        feedOptionViewModel.hideDeleteDialog()
                        onConfirm()
                        context.showToast(toastString)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.unsubscribe),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    feedOptionViewModel.hideDeleteDialog()
                }
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                )
            }
        },
    )
}