package me.ash.reader.ui.page.home.feeds

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.android.AndroidStringsHelper
import me.ash.reader.domain.data.DiffMapHolder
import me.ash.reader.domain.data.FilterState
import me.ash.reader.domain.data.FilterStateUseCase
import me.ash.reader.domain.data.GroupWithFeedsListUseCase
import me.ash.reader.domain.service.SyncWorker
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.DefaultDispatcher
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.preference.SettingsProvider
import javax.inject.Inject

private const val TAG = "FeedsViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedsViewModel @Inject constructor(
    private val accountService: AccountService,
    private val rssService: RssService,
    private val workManager: WorkManager,
    private val androidStringsHelper: AndroidStringsHelper,
    @DefaultDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope
    private val applicationScope: CoroutineScope,
    private val settingsProvider: SettingsProvider,
    private val diffMapHolder: DiffMapHolder,
    private val filterStateUseCase: FilterStateUseCase,
    private val groupWithFeedsListUseCase: GroupWithFeedsListUseCase,
) : ViewModel() {

    private val _feedsUiState =
        MutableStateFlow(FeedsUiState())
    val feedsUiState: StateFlow<FeedsUiState> = _feedsUiState.asStateFlow()

    val syncWorkLiveData = workManager.getWorkInfosByTagLiveData(SyncWorker.SYNC_TAG)

    val filterStateFlow = filterStateUseCase.filterStateFlow
    val groupWithFeedsListFlow = groupWithFeedsListUseCase.groupWithFeedListFlow

    var currentJob: Job? = null

    fun sync() {
        applicationScope.launch(ioDispatcher) {
            rssService.get().doSyncOneTime()
        }
    }

    fun commitDiffs() = diffMapHolder.commitDiffsToDb()

    fun changeFilter(filterState: FilterState) {
        filterStateUseCase.updateFilterState(filterState)
    }

    init {
        val accountFlow = accountService.currentAccountFlow
        viewModelScope.launch {
            accountFlow.collect { account ->
                _feedsUiState.update { it.copy(account = account) }
            }
        }
        viewModelScope.launch {
            filterStateUseCase.filterStateFlow.mapLatest { it.filter }
                .combine(accountFlow) { filter, account ->
                    filter
                }
                .collect {
                    currentJob?.cancel()
                    currentJob = when (it) {
                        Filter.Unread -> pullUnreadFeeds()
                        Filter.Starred -> pullStarredFeeds()
                        else -> pullAllFeeds()
                    }
                }
        }
    }

    private fun pullAllFeeds(): Job {
        val articleCountMapFlow =
            rssService.get().pullImportant(isStarred = false, isUnread = false)

        return viewModelScope.launch {
            launch {
                articleCountMapFlow.mapLatest {
                    val sum = it.values.sum()
                    androidStringsHelper.getQuantityString(R.plurals.all_desc, sum, sum)
                }.flowOn(defaultDispatcher).collect { text ->
                    _feedsUiState.update { it.copy(importantSum = text) }
                }
            }
        }
    }

    private fun pullStarredFeeds(): Job {
        val starredCountMap = rssService.get().pullImportant(isStarred = true, isUnread = false)

        return viewModelScope.launch {
            starredCountMap.mapLatest {
                val sum = it.values.sum()
                androidStringsHelper.getQuantityString(R.plurals.starred_desc, sum, sum)
            }.flowOn(defaultDispatcher).collect { text ->
                _feedsUiState.update { it.copy(importantSum = text) }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun pullUnreadFeeds(): Job {
        val unreadCountMapFlow = rssService.get().pullImportant(isStarred = false, isUnread = true)

        return viewModelScope.launch {
            diffMapHolder.diffMapSnapshotFlow
                .combine(
                    unreadCountMapFlow
                ) { diffMap, unreadCountMap ->
                    val sum = unreadCountMap.values.sum()
                    val combinedSum =
                        sum + diffMap.values.sumOf { if (it.isUnread) 1.toInt() else -1 } // KT-46360
                    androidStringsHelper.getQuantityString(
                        R.plurals.unread_desc,
                        combinedSum,
                        combinedSum
                    )
                }.debounce(200L).flowOn(defaultDispatcher).collect { text ->
                    _feedsUiState.update { it.copy(importantSum = text) }
                }
        }
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    fun pullFeeds(filterState: FilterState, hideEmptyGroups: Boolean) {
//        val isStarred = filterState.filter.isStarred()
//        val isUnread = filterState.filter.isUnread()
//        _feedsUiState.update {
//            val important = rssService.get().pullImportant(isStarred, isUnread)
//            it.copy(
////                importantSum = important
////                    .mapLatest {
////                        (it["sum"] ?: 0).run {
////                            androidStringsHelper.getQuantityString(
////                                when {
////                                    isStarred -> R.plurals.starred_desc
////                                    isUnread -> R.plurals.unread_desc
////                                    else -> R.plurals.all_desc
////                                },
////                                this,
////                                this
////                            )
////                        }
////                    }.flowOn(defaultDispatcher),
//                groupWithFeedList = combine(
//                    important,
//                    rssService.get().pullFeeds()
//                ) { importantMap, groupWithFeedList ->
//                    val groupIterator = groupWithFeedList.iterator()
//                    while (groupIterator.hasNext()) {
//                        val groupWithFeed = groupIterator.next()
//                        val groupImportant = importantMap[groupWithFeed.group.id] ?: 0
//                        if (hideEmptyGroups && (isStarred || isUnread) && groupImportant == 0) {
//                            groupIterator.remove()
//                            continue
//                        }
//                        groupWithFeed.group.important = groupImportant
//                        val feedIterator = groupWithFeed.feeds.iterator()
//                        while (feedIterator.hasNext()) {
//                            val feed = feedIterator.next()
//                            val feedImportant = importantMap[feed.id] ?: 0
//                            groupWithFeed.group.feeds++
//                            if (hideEmptyGroups && (isStarred || isUnread) && feedImportant == 0) {
//                                feedIterator.remove()
//                                continue
//                            }
//                            feed.important = feedImportant
//                        }
//                    }
//                    groupWithFeedList
//                }.mapLatest { list ->
//                    list.filter { (group, feeds) ->
//                        group.id != feedsUiState.value.account?.id?.getDefaultGroupId() || feeds.isNotEmpty()
//                    }
//                }.flowOn(defaultDispatcher),
//            )
//        }
//    }
}

data class FeedsUiState(
    val account: Account? = null,
    val importantSum: String = "",
    val listState: LazyListState = LazyListState(),
    val groupsVisible: SnapshotStateMap<String, Boolean> = mutableStateMapOf(),
)
