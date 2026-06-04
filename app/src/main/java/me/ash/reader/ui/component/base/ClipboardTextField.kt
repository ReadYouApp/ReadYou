package me.ash.reader.ui.component.base

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun ClipboardTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    placeholder: String = "",
    isPassword: Boolean = false,
    errorText: String = "",
    imeAction: ImeAction = ImeAction.Done,
    onConfirm: (String) -> Unit = {},
) {
    Column(modifier = Modifier) {
        Spacer(modifier = Modifier.height(10.dp))
        RYTextField2(
            modifier = modifier,
            readOnly = readOnly,
            state = state,
            singleLine = singleLine,
            placeholder = placeholder,
            isPassword = isPassword,
            errorMessage = errorText,
            onKeyboardAction =
                if (imeAction != ImeAction.Default || imeAction != ImeAction.None) {
                    KeyboardActionHandler { onConfirm(state.text.toString()) }
                } else null,
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}
