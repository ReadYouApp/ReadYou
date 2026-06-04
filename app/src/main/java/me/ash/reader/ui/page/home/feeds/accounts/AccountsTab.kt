package me.ash.reader.ui.page.home.feeds.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.R
import me.ash.reader.domain.model.account.Account
import me.ash.reader.ui.component.base.RYDialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountsTab(
    modifier: Modifier = Modifier,
    visible: Boolean = false,
    currentAccountId: Int?,
    accounts: List<Account>,
    onAccountSwitch: (Account) -> Unit = {},
    onClickSettings: () -> Unit = {},
    onClickManage: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    RYDialog(
        modifier = modifier,
        visible = visible,
        onDismissRequest = onDismissRequest,
        icon = { Icon(imageVector = Icons.Outlined.People, contentDescription = null) },
        title = { Text(text = stringResource(R.string.accounts)) },
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            ) {
                accounts.forEach { account ->
                    val selected = account.id == currentAccountId
                    Column(
                        modifier =
                            Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    if (selected) onClickSettings() else onAccountSwitch(account)
                                }
                                .padding(8.dp)
                    ) {
                        IconContainer(selected = selected) {
                            AccountIcon(account = account, selected = selected)
                        }
                        AccountLabel(account.name)
                    }
                }
                Column(
                    modifier =
                        Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(onClick = onClickManage)
                            .padding(8.dp)
                ) {
                    IconContainer(selected = false) {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(24.dp))
                    }
                    AccountLabel(stringResource(R.string.add))
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun IconContainer(
    modifier: Modifier = Modifier,
    selected: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primaryFixed
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    }
                ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun AccountIcon(account: Account, selected: Boolean) {
    val icon = account.type.toIcon().takeIf { it is ImageVector }?.let { it as ImageVector }
    val iconPainter = account.type.toIcon().takeIf { it is Painter }?.let { it as Painter }
    val contentColor =
        if (selected) MaterialTheme.colorScheme.onPrimaryFixed
        else MaterialTheme.colorScheme.onSurfaceVariant

    if (icon != null) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = account.name,
            tint = contentColor,
        )
    } else {
        iconPainter?.let {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = it,
                contentDescription = account.name,
                tint = contentColor,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AccountLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .padding(top = 6.dp)
            .width(52.dp),
        textAlign = TextAlign.Center,
        text = text,
        style = MaterialTheme.typography.bodySmall.merge(fontSize = 11.sp, letterSpacing = 0.sp),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
