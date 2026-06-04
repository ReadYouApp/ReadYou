package me.ash.reader.ui.page.settings.accounts.addition

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.component.base.RYOutlinedTextField2
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.showToast
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun AddLocalAccountDialog(
    onBack: () -> Unit,
    onNavigateToAccountDetails: (Int) -> Unit,
    viewModel: AdditionViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState = viewModel.additionUiState.collectAsStateValue()
    val accountUiState = accountViewModel.accountUiState.collectAsStateValue()

    val nameState = rememberTextFieldState()

    val onConfirm = {
        if (!accountUiState.isLoading && nameState.text.isNotBlank()) {
            accountViewModel.addAccount(
                Account(type = AccountType.Local, name = nameState.text.toString())
            ) { account, exception ->
                if (account == null) {
                    context.showToast(exception?.message ?: "Not valid credentials")
                } else {
                    viewModel.hideAddLocalAccountDialog()
                    onBack()
                    onNavigateToAccountDetails(account.id!!)
                }
            }
        }
    }

    RYDialog(
        modifier = Modifier.padding(horizontal = 44.dp),
        visible = uiState.addLocalAccountDialogVisible,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            focusManager.clearFocus()
            accountViewModel.cancelAdd()
            viewModel.hideAddLocalAccountDialog()
        },
        icon = {
            if (accountUiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Rounded.RssFeed,
                    contentDescription = stringResource(R.string.local),
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.local),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(10.dp))
                RYOutlinedTextField2(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = accountUiState.isLoading,
                    state = nameState,
                    label = stringResource(R.string.name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    onKeyboardAction = { onConfirm() },
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        },
        confirmButton = {
            TextButton(
                enabled = !accountUiState.isLoading && nameState.text.isNotBlank(),
                onClick = {
                    focusManager.clearFocus()
                    onConfirm()
                },
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    accountViewModel.cancelAdd()
                    viewModel.hideAddLocalAccountDialog()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
