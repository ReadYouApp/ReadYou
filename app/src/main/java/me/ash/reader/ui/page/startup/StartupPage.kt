package me.ash.reader.ui.page.startup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ireward.htmlcompose.HtmlText
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.DynamicSVGImage
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.Tips
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.svg.SVGString
import me.ash.reader.ui.svg.WELCOME

@Composable
fun StartupPage(
    //    navController: NavHostController,
    onNavigateToFeeds: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tosVisible by remember { mutableStateOf(false) }

    RYScaffold(
        content = {
            LazyColumn(modifier = Modifier.navigationBarsPadding()) {
                item {
                    Spacer(modifier = Modifier.height(64.dp))
                    DisplayText(text = stringResource(R.string.welcome), desc = "")
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    DynamicSVGImage(
                        modifier = Modifier.padding(horizontal = 60.dp),
                        svgImageString = SVGString.WELCOME,
                        contentDescription = stringResource(R.string.color_and_style),
                    )
                }
                item {
                    Tips(
                        modifier = Modifier.padding(top = 40.dp),
                        text = stringResource(R.string.tos_tips),
                    )
                }
                item {
                    TextButton(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = { tosVisible = true },
                    ) {
                        HtmlText(
                            text = stringResource(R.string.browse_tos_tips),
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.outline
                                ),
                        )
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        },
        bottomBar = null,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.navigationBarsPadding(),
                onClick = {
                    onNavigateToFeeds()
                    scope.launch { context.dataStore.put(DataStoreKey.isFirstLaunch, false) }
                },
                icon = { Icon(Icons.Rounded.CheckCircleOutline, stringResource(R.string.agree)) },
                text = { Text(text = stringResource(R.string.agree)) },
            )
        },
    )

    RYDialog(
        visible = tosVisible,
        onDismissRequest = { tosVisible = false },
        icon = {
            Icon(
                imageVector = Icons.Rounded.Balance,
                contentDescription = stringResource(R.string.change_log),
            )
        },
        title = { Text(text = stringResource(R.string.terms_of_service)) },
        text = {
            SelectionContainer {
                HtmlText(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    text = stringResource(R.string.tos_content),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onNavigateToFeeds()
                    scope.launch { context.dataStore.put(DataStoreKey.isFirstLaunch, false) }
                }
            ) {
                Text(text = stringResource(R.string.agree))
            }
        },
        dismissButton = {
            TextButton(onClick = { tosVisible = false }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}
