package me.ash.reader.ui.page.settings.translation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalTranslateArticle
import me.ash.reader.infrastructure.preference.LocalTranslateTitle
import me.ash.reader.infrastructure.preference.LocalTranslateTargetLanguage
import me.ash.reader.infrastructure.preference.LocalTranslateWifiOnly
import me.ash.reader.infrastructure.preference.TranslateArticlePreference
import me.ash.reader.infrastructure.preference.not
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import java.util.Locale

@Composable
fun TranslationPage(
    onBack: () -> Unit,
    onNavigateToTargetLanguage: () -> Unit,
    viewModel: TranslationSettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val translateArticle = LocalTranslateArticle.current
    val translateTitle = LocalTranslateTitle.current
    val translateTargetLanguage = LocalTranslateTargetLanguage.current
    val translateWifiOnly = LocalTranslateWifiOnly.current
    val downloadedModels by viewModel.downloadedModels.collectAsStateWithLifecycle()
    val modelToDelete by viewModel.modelToDelete.collectAsStateWithLifecycle()

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
                    DisplayText(text = stringResource(R.string.translation), desc = "")
                }
                item {
                    SettingItem(
                        title = stringResource(R.string.translate_article),
                        desc = stringResource(R.string.translation_desc),
                        onClick = {
                            (!translateArticle).put(context, scope)
                        },
                    ) {
                        Switch(
                            checked = translateArticle.value,
                            onCheckedChange = {
                                (!translateArticle).put(context, scope)
                            }
                        )
                    }
                }
                item {
                    SettingItem(
                        title = stringResource(R.string.translate_title),
                        onClick = {
                            (!translateTitle).put(context, scope)
                        },
                    ) {
                        Switch(
                            checked = translateTitle.value,
                            onCheckedChange = {
                                (!translateTitle).put(context, scope)
                            }
                        )
                    }
                }
                item {
                    SettingItem(
                        title = stringResource(R.string.translate_target_language),
                        desc = translateTargetLanguage.toDesc(),
                        icon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        onClick = onNavigateToTargetLanguage
                    )
                }
                item {
                    SettingItem(
                        title = stringResource(R.string.translate_wifi_only),
                        onClick = {
                            (!translateWifiOnly).put(context, scope)
                        },
                    ) {
                        Switch(
                            checked = translateWifiOnly.value,
                            onCheckedChange = {
                                (!translateWifiOnly).put(context, scope)
                            }
                        )
                    }
                }
                item {
                    DisplayText(text = stringResource(R.string.manage_models), desc = "")
                }
                if (downloadedModels.isEmpty()) {
                    item {
                        SettingItem(
                            title = stringResource(R.string.no_downloaded_models),
                            onClick = {}
                        )
                    }
                } else {
                    items(downloadedModels.size) { index ->
                        val model = downloadedModels[index]
                        val name = Locale.forLanguageTag(model).displayName
                        SettingItem(
                            title = name,
                            desc = model,
                            onClick = {}
                        ) {
                            IconButton(onClick = { viewModel.requestDeleteModel(model) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.delete_model)
                                )
                            }
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

    if (modelToDelete != null) {
        RYDialog(
            visible = true,
            onDismissRequest = { viewModel.cancelDelete() },
            title = {
                Text(text = stringResource(R.string.delete_model_title))
            },
            text = {
                val name = Locale.forLanguageTag(modelToDelete!!).displayName
                Text(text = stringResource(R.string.delete_model_confirmation, name))
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cancelDelete() }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}
