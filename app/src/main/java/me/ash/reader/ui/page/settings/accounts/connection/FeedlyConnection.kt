package me.ash.reader.ui.page.settings.accounts.connection

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.security.FeedlySecurityKey
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.ext.mask
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

@Composable
fun LazyItemScope.FeedlyConnection(
    account: Account,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val securityKey by remember { derivedStateOf { FeedlySecurityKey(account.securityKey) } }

    var tokenMask by remember { mutableStateOf(securityKey.accessToken?.mask()) }
    var accessTokenValue by remember { mutableStateOf(securityKey.accessToken) }
    var tokenDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(securityKey.accessToken) { tokenMask = securityKey.accessToken?.mask() }

    SettingItem(
        title = stringResource(R.string.feedly_access_token),
        desc = tokenMask,
        onClick = { tokenDialogVisible = true },
    ) {}

    TextFieldDialog(
        visible = tokenDialogVisible,
        title = stringResource(R.string.feedly_access_token),
        value = accessTokenValue ?: "",
        placeholder = stringResource(R.string.feedly_access_token_hint),
        isPassword = true,
        onValueChange = { accessTokenValue = it },
        onDismissRequest = { tokenDialogVisible = false },
        onConfirm = {
            if (accessTokenValue?.isNotBlank() == true) {
                securityKey.accessToken = accessTokenValue
                save(account, viewModel, securityKey)
                tokenDialogVisible = false
            }
        },
    )
}

private fun save(account: Account, viewModel: AccountViewModel, securityKey: FeedlySecurityKey) {
    account.id?.let { viewModel.update(it) { copy(securityKey = securityKey.toString()) } }
}
