package me.ash.reader.ui.page.settings.accounts

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun AccountsPage(
    viewModel: AccountViewModel = hiltViewModel(),
    onBack: () -> Unit,
    navigateToAddAccount: () -> Unit,
    navigateToAccountDetails: (Int) -> Unit,
) {
    val context = LocalContext.current
    val uiState = viewModel.accountUiState.collectAsStateValue()
    val accounts = viewModel.accounts.collectAsStateValue(initial = emptyList())

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.accounts), desc = "")
                    Spacer(modifier = Modifier.height(16.dp))
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.list),
                    )
                }
                accounts.forEach {
                    item {
                        SettingItem(
                            title = it.name,
                            desc = it.type.toDesc(context),
                            icon = it.type.toIcon().takeIf { it is ImageVector }?.let { it as ImageVector },
                            iconPainter = it.type.toIcon().takeIf { it is Painter }?.let { it as Painter },
                            onClick = { navigateToAccountDetails(it.id!!) },
                        ) {}
                    }
                }
                item {
                    Tips(text = stringResource(R.string.accounts_tips))
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.more),
                    )
                    SettingItem(
                        title = stringResource(R.string.add_accounts),
                        desc = stringResource(R.string.add_accounts_desc),
                        icon = Icons.Outlined.PersonAdd,
                        onClick = navigateToAddAccount,
                    ) {}
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )
}

@Preview
@Composable
fun AccountsPreview() {
    AccountsPage(
        onBack = {},
        navigateToAddAccount = {},
        navigateToAccountDetails = {}
    )
}
