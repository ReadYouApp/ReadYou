package me.ash.reader.ui.page.settings.ai

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalAiBaseUrl
import me.ash.reader.infrastructure.preference.LocalAiApiKey
import me.ash.reader.infrastructure.preference.LocalAiModel
import me.ash.reader.infrastructure.preference.LocalAiSummarizationPrompt
import me.ash.reader.infrastructure.preference.LocalSettings
import me.ash.reader.infrastructure.net.ApiResult
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.RadioDialog
import me.ash.reader.ui.component.base.RadioDialogOption
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun AiSettingsPage(
    aiSettingsViewModel: AiSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val aiBaseUrl = LocalAiBaseUrl.current
    val aiApiKey = LocalAiApiKey.current
    val aiModel = LocalAiModel.current
    val aiSummarizationPrompt = LocalAiSummarizationPrompt.current
    val settings = LocalSettings.current
    
    val scope = rememberCoroutineScope()
    
    var baseUrlDialogVisible by remember { mutableStateOf(false) }
    var apiKeyDialogVisible by remember { mutableStateOf(false) }
    var modelDialogVisible by remember { mutableStateOf(false) }
    var promptDialogVisible by remember { mutableStateOf(false) }
    
    val availableModels = remember { mutableStateListOf<String>() }
    var isLoadingModels by remember { mutableStateOf(false) }
    var fetchError by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(aiBaseUrl.value, aiApiKey.value) {
        if (aiBaseUrl.value.isNotEmpty() && aiApiKey.value.isNotEmpty()) {
            isLoadingModels = true
            fetchError = null
            availableModels.clear()
            
            aiSettingsViewModel.fetchModels(
                baseUrl = aiBaseUrl.value,
                apiKey = aiApiKey.value,
                onSuccess = { models ->
                    availableModels.addAll(models)
                    isLoadingModels = false
                },
                onError = { error ->
                    fetchError = error
                    isLoadingModels = false
                }
            )
        }
    }

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
                    DisplayText(
                        text = stringResource(R.string.ai_settings),
                        desc = stringResource(R.string.ai_settings_desc)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.api_configuration)
                    )
                    SettingItem(
                        title = stringResource(R.string.ai_base_url),
                        desc = aiBaseUrl.toDesc(context),
                        onClick = {
                            baseUrlDialogVisible = true
                        }
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.ai_api_key),
                        desc = aiApiKey.toDesc(context),
                        onClick = {
                            apiKeyDialogVisible = true
                        }
                    ) {}
                }

                if (isLoadingModels) {
                    item {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.ai_fetch_models),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                if (fetchError != null) {
                    item {
                        Text(
                            text = fetchError!!,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (availableModels.isNotEmpty()) {
                    item {
                        SettingItem(
                            title = stringResource(R.string.ai_model),
                            desc = aiModel.toDesc(context),
                            onClick = {
                                modelDialogVisible = true
                            }
                        ) {}
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.summarization_settings)
                    )
                    SettingItem(
                        title = stringResource(R.string.ai_summarization_prompt),
                        desc = aiSummarizationPrompt.toDesc(context),
                        onClick = {
                            promptDialogVisible = true
                        }
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    TextFieldDialog(
        textFieldState = rememberTextFieldState(aiBaseUrl.value),
        visible = baseUrlDialogVisible,
        title = stringResource(R.string.ai_base_url),
        placeholder = stringResource(R.string.ai_base_url_hint),
        onDismissRequest = { baseUrlDialogVisible = false },
        onConfirm = { value: String ->
            aiBaseUrl.copy(value = value).put(context, scope)
            baseUrlDialogVisible = false
        }
    )

    TextFieldDialog(
        textFieldState = rememberTextFieldState(aiApiKey.value),
        visible = apiKeyDialogVisible,
        title = stringResource(R.string.ai_api_key),
        placeholder = stringResource(R.string.ai_api_key_hint),
        isPassword = true,
        onDismissRequest = { apiKeyDialogVisible = false },
        onConfirm = { value: String ->
            aiApiKey.copy(value = value).put(context, scope)
            apiKeyDialogVisible = false
        }
    )

    if (availableModels.isNotEmpty()) {
        RadioDialog(
            visible = modelDialogVisible,
            title = stringResource(R.string.ai_model),
            options = availableModels.map { model ->
                RadioDialogOption(
                    text = model,
                    selected = model == aiModel.value,
                ) {
                    aiModel.copy(value = model).put(context, scope)
                }
            },
            onDismissRequest = {
                modelDialogVisible = false
            }
        )
    }

    TextFieldDialog(
        textFieldState = rememberTextFieldState(aiSummarizationPrompt.value),
        visible = promptDialogVisible,
        title = stringResource(R.string.ai_summarization_prompt),
        placeholder = stringResource(R.string.ai_summarization_prompt_hint),
        singleLine = false,
        onDismissRequest = { promptDialogVisible = false },
        onConfirm = { value: String ->
            aiSummarizationPrompt.copy(value = value).put(context, scope)
            promptDialogVisible = false
        }
    )
}
