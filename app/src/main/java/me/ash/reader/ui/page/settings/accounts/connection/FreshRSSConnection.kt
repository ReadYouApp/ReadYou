package me.ash.reader.ui.page.settings.accounts.connection

import android.app.Activity
import android.security.KeyChain
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.security.FreshRSSSecurityKey
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.ext.mask
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

@Composable
fun LazyItemScope.FreshRSSConnection(
    account: Account,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val securityKey by remember { derivedStateOf { FreshRSSSecurityKey(account.securityKey) } }

    var passwordMask by remember { mutableStateOf(securityKey.password?.mask()) }

    var serverUrlValue by remember { mutableStateOf(securityKey.serverUrl) }
    var usernameValue by remember { mutableStateOf(securityKey.username) }
    var passwordValue by remember { mutableStateOf(securityKey.password) }

    var serverUrlDialogVisible by remember { mutableStateOf(false) }
    var usernameDialogVisible by remember { mutableStateOf(false) }
    var passwordDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(securityKey.password) { passwordMask = securityKey.password?.mask() }

    SettingItem(
        title = stringResource(R.string.server_url),
        desc = securityKey.serverUrl ?: "",
        onClick = { serverUrlDialogVisible = true },
    ) {}
    SettingItem(
        title = stringResource(R.string.username),
        desc = securityKey.username ?: "",
        onClick = { usernameDialogVisible = true },
    ) {}
    SettingItem(
        title = stringResource(R.string.password),
        desc = passwordMask,
        onClick = { passwordDialogVisible = true },
    ) {}
    SettingItem(
        title = stringResource(R.string.client_certificate),
        desc = securityKey.clientCertificateAlias,
        onClick = {
            KeyChain.choosePrivateKeyAlias(
                context as Activity,
                { alias ->
                    securityKey.clientCertificateAlias = alias
                    save(account, viewModel, securityKey)
                },
                null,
                null,
                null,
                null,
            )
        },
    ) {}

    TextFieldDialog(
        visible = serverUrlDialogVisible,
        title = stringResource(R.string.server_url),
        value = serverUrlValue ?: "",
        placeholder = "https://demo.freshrss.org/api/greader.php",
        onValueChange = { serverUrlValue = it },
        onDismissRequest = { serverUrlDialogVisible = false },
        onConfirm = {
            if (securityKey.serverUrl?.isNotBlank() == true) {
                securityKey.serverUrl = serverUrlValue
                save(account, viewModel, securityKey)
                serverUrlDialogVisible = false
            }
        },
    )

    TextFieldDialog(
        visible = usernameDialogVisible,
        title = stringResource(R.string.username),
        value = usernameValue ?: "",
        onValueChange = { usernameValue = it },
        onDismissRequest = { usernameDialogVisible = false },
        onConfirm = {
            if (securityKey.username?.isNotEmpty() == true) {
                securityKey.username = usernameValue
                save(account, viewModel, securityKey)
                usernameDialogVisible = false
            }
        },
    )

    TextFieldDialog(
        visible = passwordDialogVisible,
        title = stringResource(R.string.password),
        value = passwordValue ?: "",
        isPassword = true,
        onValueChange = { passwordValue = it },
        onDismissRequest = { passwordDialogVisible = false },
        onConfirm = {
            if (securityKey.password?.isNotEmpty() == true) {
                securityKey.password = passwordValue
                save(account, viewModel, securityKey)
                passwordDialogVisible = false
            }
        },
    )
}

private fun save(account: Account, viewModel: AccountViewModel, securityKey: FreshRSSSecurityKey) {
    account.id?.let { viewModel.update(it) { copy(securityKey = securityKey.toString()) } }
}
