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
import me.ash.reader.domain.model.account.security.MinifluxSecurityKey
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.ext.findActivity
import me.ash.reader.ui.ext.mask
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

@Composable
fun LazyItemScope.MinifluxConnection(account: Account, viewModel: AccountViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val securityKey by remember { derivedStateOf { MinifluxSecurityKey(account.securityKey) } }

    var apiTokenMask by remember { mutableStateOf(securityKey.apiToken?.mask()) }
    var passwordMask by remember { mutableStateOf(securityKey.password?.mask()) }

    var serverUrlValue by remember { mutableStateOf(securityKey.serverUrl) }
    var apiTokenValue by remember { mutableStateOf(securityKey.apiToken) }
    var usernameValue by remember { mutableStateOf(securityKey.username) }
    var passwordValue by remember { mutableStateOf(securityKey.password) }

    var serverUrlDialogVisible by remember { mutableStateOf(false) }
    var apiTokenDialogVisible by remember { mutableStateOf(false) }
    var usernameDialogVisible by remember { mutableStateOf(false) }
    var passwordDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(securityKey.apiToken) { apiTokenMask = securityKey.apiToken?.mask() }
    LaunchedEffect(securityKey.password) { passwordMask = securityKey.password?.mask() }

    SettingItem(
        title = stringResource(R.string.server_url),
        desc = securityKey.serverUrl ?: "",
        onClick = { serverUrlDialogVisible = true },
    ) {}
    if (!securityKey.apiToken.isNullOrBlank()) {
        SettingItem(
            title = "API Token",
            desc = apiTokenMask,
            onClick = { apiTokenDialogVisible = true },
        ) {}
    }
    if (!securityKey.username.isNullOrBlank() || securityKey.apiToken.isNullOrBlank()) {
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
    }
    SettingItem(
        title = stringResource(R.string.client_certificate),
        desc = securityKey.clientCertificateAlias,
        onClick = {
            KeyChain.choosePrivateKeyAlias(
                context.findActivity() ?: context as Activity,
                { alias ->
                    val key = MinifluxSecurityKey(
                        serverUrl = securityKey.serverUrl,
                        apiToken = securityKey.apiToken,
                        username = securityKey.username,
                        password = securityKey.password,
                        clientCertificateAlias = alias,
                    )
                    save(account, viewModel, key)
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
        onValueChange = { serverUrlValue = it },
        onDismissRequest = { serverUrlDialogVisible = false },
        onConfirm = {
            val key = MinifluxSecurityKey(
                serverUrl = serverUrlValue,
                apiToken = securityKey.apiToken,
                username = securityKey.username,
                password = securityKey.password,
                clientCertificateAlias = securityKey.clientCertificateAlias,
            )
            save(account, viewModel, key)
            serverUrlDialogVisible = false
        },
    )

    TextFieldDialog(
        visible = apiTokenDialogVisible,
        title = "API Token",
        value = apiTokenValue ?: "",
        onValueChange = { apiTokenValue = it },
        onDismissRequest = { apiTokenDialogVisible = false },
        onConfirm = {
            val key = MinifluxSecurityKey(
                serverUrl = securityKey.serverUrl,
                apiToken = apiTokenValue,
                username = securityKey.username,
                password = securityKey.password,
                clientCertificateAlias = securityKey.clientCertificateAlias,
            )
            save(account, viewModel, key)
            apiTokenDialogVisible = false
        },
    )

    TextFieldDialog(
        visible = usernameDialogVisible,
        title = stringResource(R.string.username),
        value = usernameValue ?: "",
        onValueChange = { usernameValue = it },
        onDismissRequest = { usernameDialogVisible = false },
        onConfirm = {
            val key = MinifluxSecurityKey(
                serverUrl = securityKey.serverUrl,
                apiToken = securityKey.apiToken,
                username = usernameValue,
                password = securityKey.password,
                clientCertificateAlias = securityKey.clientCertificateAlias,
            )
            save(account, viewModel, key)
            usernameDialogVisible = false
        },
    )

    TextFieldDialog(
        visible = passwordDialogVisible,
        title = stringResource(R.string.password),
        value = passwordValue ?: "",
        onValueChange = { passwordValue = it },
        onDismissRequest = { passwordDialogVisible = false },
        onConfirm = {
            val key = MinifluxSecurityKey(
                serverUrl = securityKey.serverUrl,
                apiToken = securityKey.apiToken,
                username = securityKey.username,
                password = passwordValue,
                clientCertificateAlias = securityKey.clientCertificateAlias,
            )
            save(account, viewModel, key)
            passwordDialogVisible = false
        },
    )
}

private fun save(account: Account, viewModel: AccountViewModel, securityKey: MinifluxSecurityKey) {
    account.id?.let { viewModel.update(it) { copy(securityKey = securityKey.toString()) } }
}

