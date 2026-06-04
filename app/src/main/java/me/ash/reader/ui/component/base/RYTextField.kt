package me.ash.reader.ui.component.base

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.room.util.copy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ash.reader.R

@OptIn(ExperimentalMaterial3Api::class)
@Deprecated("Use overloads with text field state instead")
@Composable
fun RYOutlineTextField(
    modifier: Modifier = Modifier,
    requestFocus: Boolean = true,
    readOnly: Boolean = false,
    value: String,
    label: String = "",
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    placeholder: String = "",
    errorMessage: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val keyboardOptions =
        keyboardOptions.let {
            if (it.imeAction == ImeAction.Unspecified) it.copy(imeAction = ImeAction.Next) else it
        }

    val state = rememberTextFieldState(value)
    val textFlow = snapshotFlow { state.text }

    LaunchedEffect(textFlow) { textFlow.collect { onValueChange(it.toString()) } }

    RYOutlinedTextField2(
        state = state,
        modifier = modifier,
        readOnly = readOnly,
        label = label,
        singleLine = singleLine,
        isPassword = isPassword,
        placeholder = placeholder,
        errorMessage = errorMessage,
        autoFocus = requestFocus,
        keyboardOptions = keyboardOptions,
    )
}

@Deprecated("Use overloads with state instead")
@Composable
fun RYTextField2(
    value: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    label: String = "",
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    placeholder: String = "",
    errorMessage: String = "",
    autoFocus: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
) {

    val keyboardOptions =
        keyboardOptions.let {
            if (it.imeAction == ImeAction.Unspecified) it.copy(imeAction = ImeAction.Next) else it
        }

    val state = rememberTextFieldState(value)
    val textFlow = snapshotFlow { state.text }

    LaunchedEffect(textFlow) { textFlow.collect { onValueChange(it.toString()) } }

    RYTextField2(
        state = state,
        modifier = modifier,
        readOnly = readOnly,
        label = label,
        singleLine = singleLine,
        isPassword = isPassword,
        placeholder = placeholder,
        errorMessage = errorMessage,
        autoFocus = autoFocus,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
    )
}

@Composable
fun RYTextField2(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    label: String = "",
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    placeholder: String = "",
    errorMessage: String = "",
    autoFocus: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    val focusRequester = remember { FocusRequester() }

    val keyboardOptions =
        keyboardOptions.let {
            if (it.imeAction == ImeAction.Unspecified) it.copy(imeAction = ImeAction.Next) else it
        }

    if (autoFocus) {
        LaunchedEffect(Unit) {
            delay(100) // ???
            focusRequester.requestFocus()
        }
    }

    if (isPassword) {
        var showPassword by remember { mutableStateOf(false) }

        SecureTextField(
            state = state,
            colors = textFieldColors(),
            label =
                if (label.isEmpty()) null
                else {
                    { Text(label) }
                },
            modifier = modifier.focusRequester(focusRequester),
            trailingIcon = {
                PasswordVisibilityButton(
                    showPassword = showPassword,
                    onValueChange = { showPassword = it },
                )
            },
            textObfuscationMode =
                if (showPassword) TextObfuscationMode.Visible
                else TextObfuscationMode.RevealLastTyped,
            placeholder = { Text(text = placeholder) },
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            isError = errorMessage.isNotEmpty(),
            supportingText =
                if (errorMessage.isNotEmpty()) {
                    { Text(errorMessage) }
                } else null,
        )
    } else {
        TextField(
            state = state,
            modifier = modifier.focusRequester(focusRequester),
            colors = textFieldColors(),
            label =
                if (label.isEmpty()) null
                else {
                    { Text(label) }
                },
            lineLimits =
                if (singleLine) TextFieldLineLimits.SingleLine else TextFieldLineLimits.MultiLine(),
            trailingIcon = {
                if (state.text.isNotEmpty()) {
                    ClearButton(state)
                } else {
                    PasteButton(state)
                }
            },
            isError = errorMessage.isNotEmpty(),
            placeholder = { Text(text = placeholder) },
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            readOnly = readOnly,
            supportingText =
                if (errorMessage.isNotEmpty()) {
                    { Text(errorMessage) }
                } else null,
        )
    }
}

@Composable
fun RYOutlinedTextField2(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    label: String = "",
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    placeholder: String = "",
    errorMessage: String = "",
    autoFocus: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    val focusRequester = remember { FocusRequester() }

    if (autoFocus) {
        LaunchedEffect(Unit) {
            delay(100) // ???
            focusRequester.requestFocus()
        }
    }

    if (isPassword) {
        var showPassword by remember { mutableStateOf(false) }

        OutlinedSecureTextField(
            state = state,
            label =
                if (label.isEmpty()) null
                else {
                    { Text(label) }
                },
            modifier = modifier.focusRequester(focusRequester),
            trailingIcon = {
                PasswordVisibilityButton(
                    showPassword = showPassword,
                    onValueChange = { showPassword = it },
                )
            },
            textObfuscationMode =
                if (showPassword) TextObfuscationMode.Visible
                else TextObfuscationMode.RevealLastTyped,
            placeholder = { Text(text = placeholder) },
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            isError = errorMessage.isNotEmpty(),
            supportingText =
                if (errorMessage.isNotEmpty()) {
                    { Text(errorMessage) }
                } else null,
        )
    } else {
        OutlinedTextField(
            state = state,
            modifier = modifier.focusRequester(focusRequester),
            label =
                if (label.isEmpty()) null
                else {
                    { Text(label) }
                },
            lineLimits =
                if (singleLine) TextFieldLineLimits.SingleLine else TextFieldLineLimits.MultiLine(),
            trailingIcon = {
                if (state.text.isNotEmpty()) {
                    ClearButton(state)
                } else {
                    PasteButton(state)
                }
            },
            isError = errorMessage.isNotEmpty(),
            placeholder = { Text(text = placeholder) },
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            readOnly = readOnly,
            supportingText =
                if (errorMessage.isNotEmpty()) {
                    { Text(errorMessage) }
                } else null,
        )
    }
}

@Composable
private fun textFieldColors(): TextFieldColors =
    TextFieldDefaults.colors(
        unfocusedContainerColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f),
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f),
    )

@Composable
private fun PasteButton(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            scope.launch {
                val clipboardText = clipboardManager.getClipEntry()?.clipData?.getItemAt(0)?.text
                if (!clipboardText.isNullOrBlank()) {
                    state.setTextAndPlaceCursorAtEnd(clipboardText.toString())
                }
            }
        },
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Rounded.ContentPaste,
            contentDescription = stringResource(R.string.paste),
            tint = tint,
        )
    }
}

@Composable
fun ClearButton(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    IconButton(onClick = { state.clearText() }, modifier = modifier) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.clear),
            tint = tint,
        )
    }
}

@Composable
fun PasswordVisibilityButton(
    showPassword: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    IconButton(onClick = { onValueChange(!showPassword) }, modifier = modifier) {
        Icon(
            imageVector =
                if (showPassword) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
            contentDescription = if (showPassword) "Show password" else "Hide password",
            tint = tint,
        )
    }
}
