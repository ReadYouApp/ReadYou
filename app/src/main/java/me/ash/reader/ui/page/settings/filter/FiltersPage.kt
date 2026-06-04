package me.ash.reader.ui.page.settings.filter

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.KeywordFiltersPreference
import me.ash.reader.infrastructure.preference.LocalKeywordFilters
import me.ash.reader.infrastructure.preference.LocalSemanticFilter
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.RYSwitch
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun FiltersPage(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keywordFilters = LocalKeywordFilters.current
    val semanticFilter = LocalSemanticFilter.current

    var addKeywordDialogVisible by remember { mutableStateOf(false) }
    var newKeyword by remember { mutableStateOf("") }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
            ) {
                navController.popBackStack()
            }
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.filters), desc = "")
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.smart_filters),
                    )
                    SettingItem(
                        title = stringResource(R.string.semantic_filtering),
                        desc = stringResource(R.string.semantic_filtering_desc),
                        enabled = false,
                        onClick = {},
                    ) {
                        RYSwitch(activated = semanticFilter.value, enable = false) {}
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.keyword_filters),
                    )
                    SettingItem(
                        title = stringResource(R.string.add_keyword),
                        desc = stringResource(R.string.add_keyword_desc),
                        onClick = {
                            newKeyword = ""
                            addKeywordDialogVisible = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.add_keyword),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(keywordFilters) { keyword ->
                    SettingItem(
                        title = keyword,
                        desc = stringResource(R.string.keyword_filter_desc),
                        onClick = {},
                    ) {
                        FeedbackIconButton(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.remove_keyword),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        ) {
                            KeywordFiltersPreference.put(
                                context = context,
                                scope = scope,
                                filters = keywordFilters - keyword,
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        },
    )

    if (addKeywordDialogVisible) {
        AlertDialog(
            onDismissRequest = { addKeywordDialogVisible = false },
            title = { Text(stringResource(R.string.add_keyword)) },
            text = {
                OutlinedTextField(
                    value = newKeyword,
                    onValueChange = { newKeyword = it },
                    label = { Text(stringResource(R.string.keyword)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newKeyword.trim()
                        if (trimmed.isNotEmpty() && trimmed !in keywordFilters) {
                            KeywordFiltersPreference.put(
                                context = context,
                                scope = scope,
                                filters = keywordFilters + trimmed,
                            )
                        }
                        addKeywordDialogVisible = false
                    },
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { addKeywordDialogVisible = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
