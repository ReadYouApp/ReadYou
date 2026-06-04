package me.ash.reader.ui.component

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import me.ash.reader.R
import me.ash.reader.ui.component.base.TextFieldDialog

@Composable
fun ChangeUrlDialog(
    visible: Boolean = false,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
) {
    if (visible){
        val textFieldState = rememberTextFieldState(value)
        LaunchedEffect(textFieldState) {
            snapshotFlow { textFieldState.text }.collect { onValueChange(it.toString()) }
        }
        TextFieldDialog(
            textFieldState = textFieldState,
            title = stringResource(R.string.change_url),
            icon = Icons.Outlined.Edit,
            placeholder = stringResource(R.string.feed_url_placeholder),
            onDismissRequest = onDismissRequest,
            onConfirm = onConfirm,
        )
    }
}
