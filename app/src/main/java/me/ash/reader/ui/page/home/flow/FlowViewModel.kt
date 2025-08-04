package me.ash.reader.ui.page.home.flow

import androidx.compose.ui.util.fastFirstOrNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
import me.ash.reader.domain.model.article.ArticleFlowItem
import me.ash.reader.domain.model.general.MarkAsReadConditions
import me.ash.reader.domain.service.RssService
import me.ash.reader.domain.data.DiffMapHolder
import me.ash.reader.domain.data.FilterState
import me.ash.reader.domain.data.FilterStateUseCase
import me.ash.reader.domain.data.GroupWithFeedsListUseCase
import me.ash.reader.domain.data.PagerData
import me.ash.reader.domain.service.GoogleReaderRssService
import me.ash.reader.domain.service.LocalRssService
import me.ash.reader.domain.service.SyncWorker
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.preference.PullToLoadNextFeedPreference
import me.ash.reader.infrastructure.preference.SettingsProvider
import java.util.Date
import javax.inject.Inject
import kotlin.collections.any

private const val TAG = "FlowViewModel"

@OptIn(FlowPreview::class)
@HiltViewModel
class FlowViewModel @Inject constructor(
    private val rssService: RssService,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val diffMapHolder: DiffMapHolder,
    private val articlePagingListUseCase: ArticlePagingListUseCase,
    private val filterStateUseCase: FilterStateUseCase,
    private val groupWithFeedsListUseCase: GroupWithFeedsListUseCase,
    private val settingsProvider: SettingsProvider,
    workManager: WorkManager,
) : ViewModel() {

    private val _flowUiState = MutableStateFlow(FlowUiState())
    val flowUiState: StateFlow<FlowUiState> = _flowUiState.asStateFlow()

    private val syncWorkerStatusFlow = workManager.getWorkInfosByTagFlow(SyncWorker.SYNC_TAG).map {
        it.any { workInfo ->
            workInfo.state == WorkInfo.State.RUNNING
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _isSyncingFlow = MutableStateFlow(false)
    val isSyncingFlow = _isSyncingFlow.asStateFlow()

    init {
        viewModelScope.launch {
            articlePagingListUseCase.pagerFlow.combine(groupWithFeedsListUseCase.groupWithFeedListFlow) { pagerData, groupWithFeedsList ->
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
                        val index = allGroupList.indexOfFirst {
                            it.id == filterState.group.id
                        }
                        if (index != -1) {
                            val nextGroup = allGroupList.subList(index, allGroupList.size)
                                .fastFirstOrNull { groupList.map { it.id }.contains(it.id) }
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
                        val index = allFeedList.indexOfFirst {
                            it.id == filterState.feed.id
                        }
                        if (index != -1) {
                            val nextFeed = allFeedList.subList(index, allFeedList.size)
                                .fastFirstOrNull { feedList.map { it.id }.contains(it.id) }
                            if (nextFeed != null) {
                                nextFilterState = filterState.copy(feed = nextFeed)
                            }
                        }
                    }
                }
                FlowUiState(nextFilterState = nextFilterState, pagerData = pagerData)
            }.collect { flowUiState ->
                _flowUiState.update {
                    it.copy(
                        nextFilterState = flowUiState.nextFilterState,
                        pagerData = flowUiState.pagerData
                    )
                }
            }
        }

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
            rssService.get().markAsRead(
                groupId = groupId,
                feedId = feedId,
                articleId = articleId,
                before = conditions.toDate(),
                isUnread = isUnread,
            )
        }
    }

    fun updateStarredStatus(
        articleId: String?,
        isStarred: Boolean,
    ) {
        applicationScope.launch(ioDispatcher) {
            if (articleId != null) {
                rssService.get().markAsStarred(
                    articleId = articleId,
                    isStarred = isStarred,
                )
            }
        }
    }

    fun markAsReadFromListByDate(
        date: Date,
        isBefore: Boolean,
    ) {
        viewModelScope.launch(ioDispatcher) {
            val items =
                articlePagingListUseCase.itemSnapshotList.filterIsInstance<ArticleFlowItem.Article>()
                    .map { it.articleWithFeed }.filter {
                        if (isBefore) {
                            date > it.article.date && it.article.isUnread
                        } else {
                            date < it.article.date && it.article.isUnread
                        }
                    }.distinctBy { it.article.id }

            diffMapHolder.updateDiff(articleWithFeed = items.toTypedArray(), isUnread = false)
        }
    }

    fun updateLastReadIndex(index: Int?) {
        _flowUiState.update { it.copy(lastReadIndex = index) }
    }

    fun loadNextFeedOrGroup() {
        viewModelScope.launch {
            if (settingsProvider.settings.pullToSwitchFeed == PullToLoadNextFeedPreference.MarkAsReadAndLoadNextFeed) {
                markAllAsRead()
            }
            flowUiState.value.nextFilterState?.let { filterStateUseCase.updateFilterState(it) }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val items =
                articlePagingListUseCase.itemSnapshotList.items.filterIsInstance<ArticleFlowItem.Article>()
                    .map { it.articleWithFeed }

            diffMapHolder.updateDiff(
                articleWithFeed = items.toTypedArray(), isUnread = false
            )
        }
    }


    fun sync() {
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
                is LocalRssService -> service.doSyncOneTime(
                    feedId = filterState.feed?.id, groupId = filterState.group?.id
                )

                is GoogleReaderRssService -> service.doSyncOneTime(
                    feedId = filterState.feed?.id, groupId = filterState.group?.id
                )

                else -> service.doSyncOneTime()
            }
        }
    }

}

data class FlowUiState(
    val lastReadIndex: Int? = null,
    val nextFilterState: FilterState? = null,
    val pagerData: PagerData = PagerData()
)