package me.ash.reader.ui.page.home.feeds.drawer.group

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.interaction.alphaIndicationSelectable

@Composable
private fun SingleChoiceItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .alphaIndicationSelectable(selected = selected, onClick = onClick)
                .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, interactionSource = interactionSource)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            description?.let {
                Text(it, modifier = Modifier, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
fun AllAllowNotificationDialog(
    groupName: String,
    groupOptionViewModel: GroupOptionViewModel = hiltViewModel(),
    onConfirm: () -> Unit,
) {
    val groupOptionUiState = groupOptionViewModel.groupOptionUiState.collectAsStateValue()

    if (groupOptionUiState.allAllowNotificationDialogVisible) {
        var enabled by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { groupOptionViewModel.hideAllAllowNotificationDialog() },
            confirmButton = {
                ApplyButton(
                    onClick = {
                        groupOptionViewModel.allAllowNotification(enabled) {
                            groupOptionViewModel.hideAllAllowNotificationDialog()
                            onConfirm()
                        }
                    }
                )
            },
            dismissButton = {
                CancelButton(onClick = { groupOptionViewModel.hideAllAllowNotificationDialog() })
            },
            title = { Text(stringResource(R.string.notifications)) },
            icon = { Icon(imageVector = Icons.Outlined.Notifications, contentDescription = null) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        stringResource(
                            R.string.group_configuration_description,
                            stringResource(R.string.notifications),
                            groupName,
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SingleChoiceItem(
                        title = stringResource(R.string.disable),
                        selected = !enabled,
                    ) {
                        enabled = false
                    }
                    SingleChoiceItem(
                        title = stringResource(R.string.enable),
                        description = stringResource(R.string.notifications_desc),
                        selected = enabled,
                    ) {
                        enabled = true
                    }
                }
            },
        )
    }
}

@Composable
fun AllOpenInBrowserDialog(
    groupName: String,
    groupOptionViewModel: GroupOptionViewModel = hiltViewModel(),
    onConfirm: () -> Unit,
) {
    val groupOptionUiState = groupOptionViewModel.groupOptionUiState.collectAsStateValue()

    if (groupOptionUiState.allOpenInBrowserDialogVisible) {
        var enabled by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { groupOptionViewModel.hideAllOpenInBrowserDialog() },
            confirmButton = {
                ApplyButton(
                    onClick = {
                        groupOptionViewModel.allOpenInBrowser(enabled) {
                            groupOptionViewModel.hideAllOpenInBrowserDialog()
                            onConfirm()
                        }
                    }
                )
            },
            dismissButton = {
                CancelButton(onClick = { groupOptionViewModel.hideAllOpenInBrowserDialog() })
            },
            title = { Text(stringResource(R.string.open_in_browser)) },
            icon = { Icon(imageVector = Icons.Outlined.OpenInBrowser, contentDescription = null) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        stringResource(
                            R.string.group_configuration_description,
                            stringResource(R.string.open_in_browser),
                            groupName,
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SingleChoiceItem(
                        title = stringResource(R.string.disable),
                        selected = !enabled,
                    ) {
                        enabled = false
                    }
                    SingleChoiceItem(
                        title = stringResource(R.string.enable),
                        description = stringResource(R.string.open_in_browser_desc),
                        selected = enabled,
                    ) {
                        enabled = true
                    }
                }
            },
        )
    }
}

@Composable
fun AllParseFullContentDialog(
    groupName: String,
    groupOptionViewModel: GroupOptionViewModel = hiltViewModel(),
    onConfirm: () -> Unit,
) {
    val groupOptionUiState = groupOptionViewModel.groupOptionUiState.collectAsStateValue()

    if (groupOptionUiState.allParseFullContentDialogVisible) {
        var enabled by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { groupOptionViewModel.hideAllParseFullContentDialog() },
            confirmButton = {
                ApplyButton(
                    onClick = {
                        groupOptionViewModel.allParseFullContent(enabled) {
                            groupOptionViewModel.hideAllParseFullContentDialog()
                            onConfirm()
                        }
                    }
                )
            },
            dismissButton = {
                CancelButton(onClick = { groupOptionViewModel.hideAllParseFullContentDialog() })
            },
            title = { Text(stringResource(R.string.parse_full_content)) },
            icon = {
                Icon(imageVector = Icons.AutoMirrored.Outlined.Article, contentDescription = null)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        stringResource(
                            R.string.group_configuration_description,
                            stringResource(R.string.parse_full_content),
                            groupName,
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SingleChoiceItem(
                        title = stringResource(R.string.disable),
                        selected = !enabled,
                    ) {
                        enabled = false
                    }
                    SingleChoiceItem(
                        title = stringResource(R.string.enable),
                        description = stringResource(R.string.parse_full_content_desc),
                        selected = enabled,
                    ) {
                        enabled = true
                    }
                }
            },
        )
    }
}

@Composable
private fun ApplyButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) { Text(stringResource(R.string.apply)) }
}

@Composable
private fun CancelButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier) { Text(stringResource(R.string.cancel)) }
}
