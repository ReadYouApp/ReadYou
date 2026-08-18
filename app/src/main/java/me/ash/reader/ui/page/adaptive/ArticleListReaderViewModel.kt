package me.ash.reader.ui.page.adaptive

import android.net.Uri
import androidx.compose.ui.util.fastFirstOrNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject
import kotlin.collections.any
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.domain.data.ArticlePagingListUseCase
import me.ash.reader.domain.data.DiffMapHolder
import me.ash.reader.domain.data.FilterState
import me.ash.reader.domain.data.FilterStateUseCase
import me.ash.reader.domain.data.GroupWithFeedsListUseCase
import me.ash.reader.domain.data.PagerData
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.article.ArticleFlowItem
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.general.MarkAsReadConditions
import me.ash.reader.domain.service.GoogleReaderRssService
import me.ash.reader.domain.service.LocalRssService
import me.ash.reader.domain.service.RssService
import me.ash.reader.domain.service.SyncWorker
import me.ash.reader.infrastructure.android.AndroidImageDownloader
import me.ash.reader.infrastructure.android.TextToSpeechManager
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.preference.PullToLoadNextFeedPreference
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.infrastructure.rss.ReaderCacheHelper
import me.ash.reader.infrastructure.translation.TranslationService
import timber.log.Timber

private const val TAG = "FlowViewModel"

@OptIn(FlowPreview::class)
@HiltViewModel()
class ArticleListReaderViewModel
@Inject
constructor(
    private val rssService: RssService,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val applicationScope: CoroutineScope,
    val diffMapHolder: DiffMapHolder,
    private val filterStateUseCase: FilterStateUseCase,
    private val groupWithFeedsListUseCase: GroupWithFeedsListUseCase,
    private val settingsProvider: SettingsProvider,
    private val readerCacheHelper: ReaderCacheHelper,
    val textToSpeechManager: TextToSpeechManager,
    private val imageDownloader: AndroidImageDownloader,
    private val articleListUseCase: ArticlePagingListUseCase,
    val translationService: TranslationService,
    workManager: WorkManager,
) : ViewModel() {

    val flowUiState: StateFlow<FlowUiState?> =
        articleListUseCase.pagerFlow
            .combine(groupWithFeedsListUseCase.groupWithFeedListFlow) {
                pagerData,
                groupWithFeedsList ->
                val filterState = pagerData.filterState
                var nextFilterState: FilterState? = null
                if (filterState.group != null) {
                    val groupList = groupWithFeedsList.map { it.group }
                    val index = groupList.indexOfFirst { it.id == filterState.group.id }
                    if (index != -1) {
                        val nextGroup = groupList.getOrNull(index + 1)
                        if (nextGroup != null) {
                            nextFilterState = filterState.copy(group = nextGroup)
                        }
                    } else {
                        val allGroupList =
                            rssService.get().queryAllGroupWithFeeds().map { it.group }
                        val index = allGroupList.indexOfFirst { it.id == filterState.group.id }
                        if (index != -1) {
                            val nextGroup =
                                allGroupList.subList(index, allGroupList.size).fastFirstOrNull {
                                    groupList.map { it.id }.contains(it.id)
                                }
                            if (nextGroup != null) {
                                nextFilterState = filterState.copy(group = nextGroup)
                            }
                        }
                    }
                } else if (filterState.feed != null) {
                    val feedList = groupWithFeedsList.flatMap { it.feeds }
                    val index = feedList.indexOfFirst { it.id == filterState.feed.id }
                    if (index != -1) {
                        val nextFeed = feedList.getOrNull(index + 1)
                        if (nextFeed != null) {
                            nextFilterState = filterState.copy(feed = nextFeed)
                        }
                    } else {
                        val allFeedList =
                            rssService.get().queryAllGroupWithFeeds().flatMap { it.feeds }
                        val index = allFeedList.indexOfFirst { it.id == filterState.feed.id }
                        if (index != -1) {
                            val nextFeed =
                                allFeedList.subList(index, allFeedList.size).fastFirstOrNull {
                                    feedList.map { it.id }.contains(it.id)
                                }
                            if (nextFeed != null) {
                                nextFilterState = filterState.copy(feed = nextFeed)
                            }
                        }
                    }
                }
                FlowUiState(nextFilterState = nextFilterState, pagerData = pagerData)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val syncWorkerStatusFlow =
        workManager
            .getWorkInfosByTagFlow(SyncWorker.SYNC_TAG)
            .map { it.any { workInfo -> workInfo.state == WorkInfo.State.RUNNING } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _isSyncingFlow = MutableStateFlow(false)
    val isSyncingFlow = _isSyncingFlow.asStateFlow()

    init {
        viewModelScope.launch {
            syncWorkerStatusFlow.debounce(500L).collect { _isSyncingFlow.value = it }
        }
    }

    fun updateReadStatus(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        conditions: MarkAsReadConditions,
        isUnread: Boolean,
    ) {
        applicationScope.launch(ioDispatcher) {
            rssService
                .get()
                .markAsRead(
                    groupId = groupId,
                    feedId = feedId,
                    articleId = articleId,
                    before = conditions.toDate(),
                    isUnread = isUnread,
                )
        }
    }

    fun updateStarredStatus(articleId: String?, isStarred: Boolean) {
        applicationScope.launch(ioDispatcher) {
            if (articleId != null) {
                rssService.get().markAsStarred(articleId = articleId, isStarred = isStarred)
            }
        }
    }

    fun markAsReadFromListByDate(date: Date, isBefore: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            val items =
                articleListUseCase.itemSnapshotList
                    .filterIsInstance<ArticleFlowItem.Article>()
                    .map { it.articleWithFeed }
                    .filter {
                        if (isBefore) {
                            date > it.article.date && it.article.isUnread
                        } else {
                            date < it.article.date && it.article.isUnread
                        }
                    }
                    .distinctBy { it.article.id }

            diffMapHolder.updateDiff(articleWithFeed = items.toTypedArray(), isUnread = false)
        }
    }

    fun loadNextFeedOrGroup() {
        viewModelScope.launch {
            if (
                settingsProvider.settings.pullToSwitchFeed ==
                    PullToLoadNextFeedPreference.MarkAsReadAndLoadNextFeed
            ) {
                markAllAsRead()
            }
            flowUiState.value?.nextFilterState?.let { filterStateUseCase.updateFilterState(it) }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val items =
                articleListUseCase.itemSnapshotList.items
                    .filterIsInstance<ArticleFlowItem.Article>()
                    .map { it.articleWithFeed }

            diffMapHolder.updateDiff(articleWithFeed = items.toTypedArray(), isUnread = false)
        }
    }

    fun sync() {
        diffMapHolder.commitDiffsToDb()
        viewModelScope.launch {
            _isSyncingFlow.value = true
            val isSyncing = syncWorkerStatusFlow.value
            if (!isSyncing) {
                delay(1000L)
                if (syncWorkerStatusFlow.value == false) {
                    _isSyncingFlow.value = false
                }
            }
        }
        applicationScope.launch(ioDispatcher) {
            val filterState = filterStateUseCase.filterStateFlow.value
            val service = rssService.get()
            when (service) {
                is LocalRssService ->
                    service.doSyncOneTime(
                        feedId = filterState.feed?.id,
                        groupId = filterState.group?.id,
                    )

                is GoogleReaderRssService ->
                    service.doSyncOneTime(
                        feedId = filterState.feed?.id,
                        groupId = filterState.group?.id,
                    )

                else -> service.doSyncOneTime()
            }
        }
    }

    fun resetFilter() =
        filterStateUseCase.updateFilterState(feed = null, group = null, searchContent = null)

    fun changeFilter(filterState: FilterState) {
        filterStateUseCase.updateFilterState(
            filterState.feed,
            filterState.group,
            filterState.filter,
        )
    }

    fun inputSearchContent(content: String? = null) {
        if (content != filterStateUseCase.filterStateFlow.value.searchContent)
            filterStateUseCase.updateFilterState(searchContent = content)
    }

    private val _readingUiState = MutableStateFlow(ReadingUiState())
    val readingUiState: StateFlow<ReadingUiState> = _readingUiState.asStateFlow()

    private val _readerState: MutableStateFlow<ReaderState> = MutableStateFlow(ReaderState())
    val readerStateStateFlow = _readerState.asStateFlow()

    private val currentArticle: Article?
        get() = readingUiState.value.articleWithFeed?.article

    private val currentFeed: Feed?
        get() = readingUiState.value.articleWithFeed?.feed

    fun initData(articleId: String, listIndex: Int? = null) {
        viewModelScope.launch {
            val snapshotList = articleListUseCase.itemSnapshotList

            val itemByIndex =
                listIndex?.let { snapshotList.getOrNull(it) as? ArticleFlowItem.Article }

            val itemFromList =
                if (itemByIndex != null && itemByIndex.articleWithFeed.article.id != articleId) {
                    itemByIndex
                } else {
                    snapshotList.find { item ->
                        item is ArticleFlowItem.Article &&
                            item.articleWithFeed.article.id == articleId
                    } as? ArticleFlowItem.Article
                }

            val item =
                itemByIndex?.articleWithFeed
                    ?: (itemFromList?.articleWithFeed
                        ?: rssService.get().findArticleById(articleId)!!)

            if (diffMapHolder.checkIfUnread(item)) {
                diffMapHolder.updateDiff(item, isUnread = false)
            }
            item.run {
                _readingUiState.update {
                    it.copy(articleWithFeed = this, isStarred = article.isStarred, isUnread = false)
                }
                _readerState.update {
                    it.copy(
                            articleId = article.id,
                            feedName = feed.name,
                            title = article.title,
                            author = article.author,
                            link = article.link,
                            publishedDate = article.date,
                            isTranslated = false,
                            isTranslating = false,
                            originalContent = null,
                            detectedLanguage = null,
                            needsDownloadConfirmation = false,
                            pendingSourceLanguage = null,
                            isDownloadingModel = false,
                            translationProgress = 0f,
                        )
                        .prefetchArticleId()
                        .renderContent(this)
                }
                // Detect article language in background for same-language guard
                detectArticleLanguage()
                // Auto-translate if feed has isTranslate enabled
                if (feed.isTranslate && settingsProvider.settings.translateArticle.value) {
                    translateContent()
                }
            }
        }
    }

    fun clearReadingData() {
        _readingUiState.update { ReadingUiState() }
        _readerState.update { ReaderState() }
    }

    /**
     * Detect the language of the current article content in the background.
     * Updates ReaderState.detectedLanguage so the UI can hide the translate button
     * when the article is already in the target language.
     */
    private fun detectArticleLanguage() {
        viewModelScope.launch {
            val content = _readerState.value.content.text ?: return@launch
            val detected = translationService.identifyHtmlLanguage(content)
            _readerState.update { it.copy(detectedLanguage = detected) }
        }
    }

    suspend fun ReaderState.renderContent(articleWithFeed: ArticleWithFeed): ReaderState {
        val contentState =
            if (articleWithFeed.feed.isFullContent) {
                val fullContent =
                    readerCacheHelper.readFullContent(articleWithFeed.article.id).getOrNull()
                if (fullContent != null) ReaderState.FullContent(fullContent)
                else {
                    renderFullContent()
                    ReaderState.Loading
                }
            } else ReaderState.Description(articleWithFeed.article.rawDescription)

        return copy(content = contentState)
    }

    fun renderDescriptionContent(translateTitle: Boolean = false) {
        val wasTranslated = _readerState.value.isTranslated
        _readerState.update {
            it.copy(
                content = ReaderState.Description(content = currentArticle?.rawDescription ?: ""),
                // Reset translation state — content source changed
                isTranslated = false,
                isTranslating = false,
                originalContent = null,
                title = it.originalTitle ?: it.title,
                originalTitle = null,
                translationProgress = 0f,
            )
        }
        // Auto-translate the new content if translation was active
        if (wasTranslated) {
            translateContent(translateTitle)
        }
    }

    fun renderFullContent(translateTitle: Boolean = false) {
        val wasTranslated = _readerState.value.isTranslated
        // Reset translation state before loading new content
        _readerState.update {
            it.copy(
                isTranslated = false,
                isTranslating = false,
                originalContent = null,
                title = it.originalTitle ?: it.title,
                originalTitle = null,
                translationProgress = 0f,
            )
        }
        val fetchJob =
            viewModelScope.launch {
                readerCacheHelper
                    .readOrFetchFullContent(currentArticle!!)
                    .onSuccess { content ->
                        _readerState.update {
                            it.copy(content = ReaderState.FullContent(content = content))
                        }
                        // Auto-translate after full content is loaded
                        if (wasTranslated) {
                            translateContent(translateTitle)
                        }
                    }
                    .onFailure { th ->
                        _readerState.update {
                            it.copy(content = ReaderState.Error(th.message.toString()))
                        }
                    }
            }
        viewModelScope.launch {
            delay(100L)
            if (fetchJob.isActive) {
                setLoading()
            }
        }
    }

    fun updateReadStatus(isUnread: Boolean) {
        readingUiState.value.articleWithFeed?.let {
            diffMapHolder.updateDiff(it, isUnread = isUnread)
        }
        _readingUiState.update {
            it.copy(isUnread = diffMapHolder.checkIfUnread(it.articleWithFeed!!))
        }
    }

    fun updateStarredStatus(isStarred: Boolean) {
        applicationScope.launch(ioDispatcher) {
            _readingUiState.update { it.copy(isStarred = isStarred) }
            currentArticle?.let {
                rssService.get().markAsStarred(articleId = it.id, isStarred = isStarred)
            }
        }
    }

    private fun setLoading() {
        _readerState.update { it.copy(content = ReaderState.Loading) }
    }

    fun translateContent(translateTitle: Boolean = true) {
        val currentContent = _readerState.value.content.text ?: return
        val articleId = _readerState.value.articleId
        val targetLang = translationService.getTargetLanguageTag()

        // Check cache first
        if (articleId != null) {
            val cached = translationService.getCachedTranslation(articleId, targetLang)
            if (cached != null) {
                _readerState.update {
                    it.copy(
                        originalContent = if (it.originalContent == null) it.content else it.originalContent,
                        originalTitle = if (it.originalTitle == null) it.title else it.originalTitle,
                        content = when (it.content) {
                            is ReaderState.Description -> ReaderState.Description(cached)
                            is ReaderState.FullContent -> ReaderState.FullContent(cached)
                            else -> it.content
                        },
                        isTranslated = true,
                        isTranslating = false,
                    )
                }
                // Translate title from cache path too
                if (translateTitle && !_readerState.value.originalTitle.isNullOrBlank()) {
                    viewModelScope.launch {
                        // Detect language from original content (longer text = reliable detection)
                        val sourceLanguage = translationService.identifyHtmlLanguage(currentContent)
                        if (sourceLanguage != "und" && sourceLanguage != targetLang) {
                            translationService.translateText(
                                text = _readerState.value.originalTitle!!,
                                sourceLanguage = sourceLanguage,
                                targetLanguage = targetLang,
                            ).onSuccess { translatedTitle ->
                                _readerState.update { it.copy(title = translatedTitle) }
                            }
                        }
                    }
                }
                return
            }
        }

        _readerState.update {
            it.copy(
                isTranslating = true,
                originalContent = if (it.originalContent == null) it.content else it.originalContent,
                originalTitle = if (it.originalTitle == null) it.title else it.originalTitle,
            )
        }

        viewModelScope.launch {
            // Identify source language (strip HTML first to avoid entity confusion)
            val sourceLanguage = translationService.identifyHtmlLanguage(currentContent)
            if (sourceLanguage == "und" || sourceLanguage == targetLang) {
                _readerState.update { it.copy(isTranslating = false) }
                return@launch
            }

            // Check if models are already downloaded
            val modelsReady = translationService.areModelsDownloaded(sourceLanguage, targetLang)
            if (!modelsReady) {
                // Ask user for confirmation before downloading
                _readerState.update {
                    it.copy(
                        isTranslating = false,
                        needsDownloadConfirmation = true,
                        pendingSourceLanguage = sourceLanguage,
                    )
                }
                return@launch
            }

            // Models are ready, translate directly
            translationService.translateHtml(
                htmlContent = currentContent,
                targetLanguage = targetLang,
                articleId = articleId,
                onProgress = { progress ->
                    _readerState.update { it.copy(translationProgress = progress) }
                },
            )
                .onSuccess { translated ->
                    // Translate title if enabled
                    val translatedTitle = if (translateTitle && !_readerState.value.title.isNullOrBlank()) {
                        translationService.translateText(
                            text = _readerState.value.title!!,
                            sourceLanguage = sourceLanguage,
                            targetLanguage = targetLang,
                        ).getOrDefault(_readerState.value.title!!)
                    } else {
                        _readerState.value.title
                    }
                    _readerState.update {
                        it.copy(
                            content = when (it.content) {
                                is ReaderState.Description -> ReaderState.Description(translated)
                                is ReaderState.FullContent -> ReaderState.FullContent(translated)
                                else -> it.content
                            },
                            title = translatedTitle,
                            isTranslated = true,
                            isTranslating = false,
                            translationProgress = 0f,
                        )
                    }
                }
                .onFailure {
                    _readerState.update { it.copy(isTranslating = false, translationProgress = 0f) }
                }
        }
    }

    /**
     * Called when user confirms the model download dialog.
     * Downloads models and then translates.
     */
    fun confirmDownloadAndTranslate(translateTitle: Boolean = true) {
        val currentContent = _readerState.value.content.text ?: return
        val sourceLanguage = _readerState.value.pendingSourceLanguage ?: return
        val targetLang = translationService.getTargetLanguageTag()
        val articleId = _readerState.value.articleId

        _readerState.update {
            it.copy(
                needsDownloadConfirmation = false,
                pendingSourceLanguage = null,
                isTranslating = true,
                isDownloadingModel = true,
            )
        }

        viewModelScope.launch {
            translationService.downloadAndTranslateHtml(
                htmlContent = currentContent,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLang,
                articleId = articleId,
                onProgress = { progress ->
                    _readerState.update { it.copy(translationProgress = progress) }
                },
            )
                .onSuccess { translated ->
                    // Translate title if enabled
                    val translatedTitle = if (translateTitle && !_readerState.value.title.isNullOrBlank()) {
                        translationService.translateText(
                            text = _readerState.value.title!!,
                            sourceLanguage = sourceLanguage,
                            targetLanguage = targetLang,
                        ).getOrDefault(_readerState.value.title!!)
                    } else {
                        _readerState.value.title
                    }
                    _readerState.update {
                        it.copy(
                            content = when (it.content) {
                                is ReaderState.Description -> ReaderState.Description(translated)
                                is ReaderState.FullContent -> ReaderState.FullContent(translated)
                                else -> it.content
                            },
                            title = translatedTitle,
                            isTranslated = true,
                            isTranslating = false,
                            isDownloadingModel = false,
                            translationProgress = 0f,
                        )
                    }
                }
                .onFailure {
                    _readerState.update { it.copy(isTranslating = false, isDownloadingModel = false, translationProgress = 0f) }
                }
        }
    }

    /**
     * Called when user cancels the model download dialog.
     */
    fun cancelTranslation() {
        _readerState.update {
            it.copy(
                needsDownloadConfirmation = false,
                pendingSourceLanguage = null,
                isTranslating = false,
                isDownloadingModel = false,
                translationProgress = 0f,
                // Restore original content and title
                content = it.originalContent ?: it.content,
                originalContent = null,
                title = it.originalTitle ?: it.title,
                originalTitle = null,
            )
        }
    }

    fun showOriginalContent() {
        val original = _readerState.value.originalContent ?: return
        _readerState.update {
            it.copy(
                content = original,
                isTranslated = false,
                originalContent = null,
                title = it.originalTitle ?: it.title,
                originalTitle = null,
            )
        }
    }

    fun toggleTranslation(translateTitle: Boolean = true) {
        if (_readerState.value.isTranslated) {
            showOriginalContent()
        } else {
            translateContent(translateTitle)
        }
    }

    fun ReaderState.prefetchArticleId(): ReaderState {
        val items = articleListUseCase.itemSnapshotList
        val currentId = currentArticle?.id
        val index =
            items.indexOfFirst { item ->
                item is ArticleFlowItem.Article && item.articleWithFeed.article.id == currentId
            }
        var previousArticle: ReaderState.PrefetchResult? = null
        var nextArticle: ReaderState.PrefetchResult? = null

        if (index != -1 || currentId == null) {
            val prevIterator = items.listIterator(index)
            while (prevIterator.hasPrevious()) {
                val previousIndex = prevIterator.previousIndex()
                val prev = prevIterator.previous()
                if (prev is ArticleFlowItem.Article) {
                    previousArticle =
                        ReaderState.PrefetchResult(
                            articleId = prev.articleWithFeed.article.id,
                            index = previousIndex,
                        )
                    break
                }
            }
            val nextIterator = items.listIterator(index + 1)
            while (nextIterator.hasNext()) {
                val nextIndex = nextIterator.nextIndex()
                val next = nextIterator.next()
                if (
                    next is ArticleFlowItem.Article && next.articleWithFeed.article.id != currentId
                ) {
                    nextArticle =
                        ReaderState.PrefetchResult(
                            articleId = next.articleWithFeed.article.id,
                            index = nextIndex,
                        )
                    break
                }
            }
        }

        Timber.d("$previousArticle, $nextArticle, $listIndex")
        return copy(nextArticle = nextArticle, previousArticle = previousArticle, listIndex = index)
    }

    fun downloadImage(
        url: String,
        onSuccess: (Uri) -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    ) {
        viewModelScope.launch {
            imageDownloader.downloadImage(url).onSuccess(onSuccess).onFailure(onFailure)
        }
    }
}

data class FlowUiState(val pagerData: PagerData, val nextFilterState: FilterState? = null)

data class ReadingUiState(
    val articleWithFeed: ArticleWithFeed? = null,
    val isUnread: Boolean = false,
    val isStarred: Boolean = false,
)

data class ReaderState(
    val articleId: String? = null,
    val feedName: String = "",
    val title: String? = null,
    val author: String? = null,
    val link: String? = null,
    val publishedDate: Date = Date(0L),
    val content: ContentState = Loading,
    val listIndex: Int? = null,
    val nextArticle: PrefetchResult? = null,
    val previousArticle: PrefetchResult? = null,
    val isTranslated: Boolean = false,
    val isTranslating: Boolean = false,
    val originalContent: ContentState? = null,
    val originalTitle: String? = null,
    val detectedLanguage: String? = null,
    val needsDownloadConfirmation: Boolean = false,
    val pendingSourceLanguage: String? = null,
    val isDownloadingModel: Boolean = false,
    val translationProgress: Float = 0f,
) {
    data class PrefetchResult(val articleId: String, val index: Int)

    sealed interface ContentState {
        val text: String?
            get() {
                return when (this) {
                    is Description -> content
                    is Error -> message
                    is FullContent -> content
                    Loading -> null
                }
            }
    }

    data class FullContent(val content: String) : ContentState

    data class Description(val content: String) : ContentState

    data class Error(val message: String) : ContentState

    data object Loading : ContentState
}
