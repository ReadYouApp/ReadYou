package me.ash.reader.ui.page.home.feeds.drawer.group

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
fun DeleteGroupDialog(
    groupName: String,
    groupOptionViewModel: GroupOptionViewModel = hiltViewModel(),
    onConfirm: () -> Unit,
) {
    val context = LocalContext.current
    val groupOptionUiState = groupOptionViewModel.groupOptionUiState.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val toastString = stringResource(R.string.delete_toast, groupName)

    RYDialog(
        visible = groupOptionUiState.deleteDialogVisible,
        onDismissRequest = {
            groupOptionViewModel.hideDeleteDialog()
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteForever,
                contentDescription = stringResource(R.string.delete_group),
            )
        },
        title = {
            Text(text = stringResource(R.string.delete_group))
        },
        text = {
            Text(text = stringResource(R.string.delete_group_tips, groupName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    groupOptionViewModel.delete {
                        groupOptionViewModel.hideDeleteDialog()
                        onConfirm()
                        context.showToast(toastString)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.delete),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    groupOptionViewModel.hideDeleteDialog()
                }
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                )
            }
        },
    )
}