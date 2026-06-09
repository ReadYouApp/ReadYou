package me.ash.reader.ui.page.settings.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.KeywordFiltersPreference
import me.ash.reader.infrastructure.preference.LocalKeywordFilters
import me.ash.reader.infrastructure.preference.LocalSemanticFilter
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.RYSwitch
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun FiltersPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keywords = LocalKeywordFilters.current
    val semanticFilter = LocalSemanticFilter.current

    var addDialogVisible by remember { mutableStateOf(false) }
    var newKeyword by remember { mutableStateOf("") }

    fun addKeyword() {
        val trimmed = newKeyword.trim()
        if (trimmed.isNotBlank() && !keywords.contains(trimmed)) {
            KeywordFiltersPreference.put(context, scope, keywords + trimmed)
        }
        newKeyword = ""
        addDialogVisible = false
    }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack,
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.filters), desc = "")
                }

                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.smart_filters),
                    )
                }

                item {
                    SettingItem(
                        title = stringResource(R.string.semantic_filter),
                        desc = stringResource(R.string.semantic_filter_desc),
                        enabled = false,
                        onClick = {},
                        action = {
                            RYSwitch(activated = semanticFilter, enable = false, onClick = {})
                        },
                    )
                }

                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        text = stringResource(R.string.keyword_filters),
                    )
                }

                item {
                    SettingItem(
                        title = stringResource(R.string.add_keyword),
                        desc = stringResource(R.string.add_keyword_desc),
                        icon = Icons.Outlined.Add,
                        onClick = { addDialogVisible = true },
                    )
                }

                items(keywords) { keyword ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = keyword,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = {
                                KeywordFiltersPreference.put(
                                    context, scope, keywords.filter { it != keyword }
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.remove),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    RYDialog(
        visible = addDialogVisible,
        onDismissRequest = {
            newKeyword = ""
            addDialogVisible = false
        },
        title = { Text(text = stringResource(R.string.add_keyword)) },
        text = {
            OutlinedTextField(
                value = newKeyword,
                onValueChange = { newKeyword = it },
                placeholder = { Text(text = stringResource(R.string.keyword_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { addKeyword() }),
            )
        },
        confirmButton = {
            TextButton(onClick = { addKeyword() }) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    newKeyword = ""
                    addDialogVisible = false
                }
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}
