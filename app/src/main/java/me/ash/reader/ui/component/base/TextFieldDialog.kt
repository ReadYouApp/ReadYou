package me.ash.reader.ui.component.base

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import me.ash.reader.R

@Deprecated("Use overload with TextFieldState instead")
@Composable
fun TextFieldDialog(
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    visible: Boolean,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    title: String = "",
    icon: ImageVector? = null,
    value: String = "",
    placeholder: String = "",
    isPassword: Boolean = false,
    errorText: String = "",
    dismissText: String = stringResource(R.string.cancel),
    confirmText: String = stringResource(R.string.confirm),
    onValueChange: (String) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
    imeAction: ImeAction = if (singleLine) ImeAction.Done else ImeAction.Default,
) {
    val focusManager = LocalFocusManager.current
    val textFieldState = rememberTextFieldState(value)

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text }.collect { onValueChange(it.toString()) }
    }

    RYDialog(
        modifier = modifier,
        properties = properties,
        visible = visible,
        onDismissRequest = onDismissRequest,
        icon = { icon?.let { Icon(imageVector = icon, contentDescription = title) } },
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            ClipboardTextField(
                state = textFieldState,
                modifier = modifier.fillMaxWidth(),
                readOnly = readOnly,
                singleLine = singleLine,
                placeholder = placeholder,
                isPassword = isPassword,
                errorText = errorText,
                imeAction = imeAction,
                onConfirm = onConfirm,
            )
        },
        confirmButton = {
            TextButton(
                enabled = value.isNotBlank(),
                onClick = {
                    focusManager.clearFocus()
                    onConfirm(value)
                },
            ) {
                Text(
                    text = confirmText,
                    color =
                        if (value.isNotBlank()) {
                            Color.Unspecified
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        },
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(text = dismissText) } },
    )
}

@Composable
fun TextFieldDialog(
    textFieldState: TextFieldState,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    visible: Boolean = false,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    title: String = "",
    icon: ImageVector? = null,
    placeholder: String = "",
    isPassword: Boolean = false,
    errorText: String = "",
    dismissText: String = stringResource(R.string.cancel),
    confirmText: String = stringResource(R.string.confirm),
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
    imeAction: ImeAction = if (singleLine) ImeAction.Done else ImeAction.Default,
) {
    val focusManager = LocalFocusManager.current

    RYDialog(
        modifier = modifier,
        properties = properties,
        visible = visible,
        onDismissRequest = onDismissRequest,
        icon = { icon?.let { Icon(imageVector = icon, contentDescription = title) } },
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            ClipboardTextField(
                state = textFieldState,
                modifier = modifier.fillMaxWidth(),
                readOnly = readOnly,
                singleLine = singleLine,
                placeholder = placeholder,
                isPassword = isPassword,
                errorText = errorText,
                imeAction = imeAction,
                onConfirm = onConfirm,
            )
        },
        confirmButton = {
            TextButton(
                enabled = textFieldState.text.isNotBlank(),
                onClick = {
                    focusManager.clearFocus()
                    onConfirm(textFieldState.text.toString())
                },
            ) {
                Text(
                    text = confirmText,
                    color =
                        if (textFieldState.text.isNotBlank()) {
                            Color.Unspecified
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        },
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(text = dismissText) } },
    )
}

@Composable
fun TextFieldDialog(
    textFieldState: TextFieldState,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    title: String = "",
    icon: ImageVector? = null,
    placeholder: String = "",
    isPassword: Boolean = false,
    errorText: String = "",
    dismissText: String = stringResource(R.string.cancel),
    confirmText: String = stringResource(R.string.confirm),
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
    imeAction: ImeAction = if (singleLine) ImeAction.Done else ImeAction.Default,
) {
    val focusManager = LocalFocusManager.current

    AlertDialog(
        modifier = modifier,
        properties = properties,
        onDismissRequest = onDismissRequest,
        icon = { icon?.let { Icon(imageVector = icon, contentDescription = title) } },
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            ClipboardTextField(
                state = textFieldState,
                modifier = modifier.fillMaxWidth(),
                readOnly = readOnly,
                singleLine = singleLine,
                placeholder = placeholder,
                isPassword = isPassword,
                errorText = errorText,
                imeAction = imeAction,
                onConfirm = onConfirm,
            )
        },
        confirmButton = {
            TextButton(
                enabled = textFieldState.text.isNotBlank(),
                onClick = {
                    focusManager.clearFocus()
                    onConfirm(textFieldState.text.toString())
                },
            ) {
                Text(
                    text = confirmText,
                    color =
                        if (textFieldState.text.isNotBlank()) {
                            Color.Unspecified
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        },
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(text = dismissText) } },
    )
}
