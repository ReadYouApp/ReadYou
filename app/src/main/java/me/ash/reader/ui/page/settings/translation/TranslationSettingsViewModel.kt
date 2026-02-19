package me.ash.reader.ui.page.settings.translation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.ash.reader.infrastructure.translation.TranslationService
import javax.inject.Inject

@HiltViewModel
class TranslationSettingsViewModel @Inject constructor(
    private val translationService: TranslationService
) : ViewModel() {

    private val _downloadedModels = MutableStateFlow<List<String>>(emptyList())
    val downloadedModels: StateFlow<List<String>> = _downloadedModels.asStateFlow()

    private val _modelToDelete = MutableStateFlow<String?>(null)
    val modelToDelete: StateFlow<String?> = _modelToDelete.asStateFlow()

    init {
        refreshDownloadedModels()
    }

    private fun refreshDownloadedModels() {
        viewModelScope.launch {
            _downloadedModels.value = translationService.getDownloadedModels()
        }
    }

    fun requestDeleteModel(languageTag: String) {
        _modelToDelete.value = languageTag
    }

    fun cancelDelete() {
        _modelToDelete.value = null
    }

    fun confirmDelete() {
        val model = _modelToDelete.value ?: return
        viewModelScope.launch {
            runCatching {
                translationService.deleteModel(model)
            }.onSuccess {
                refreshDownloadedModels()
                _modelToDelete.value = null
            }
        }
    }
}
