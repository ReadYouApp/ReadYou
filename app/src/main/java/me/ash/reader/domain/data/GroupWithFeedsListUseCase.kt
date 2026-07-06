package me.ash.reader.domain.data

import androidx.compose.ui.util.fastFilteredMap
import androidx.compose.ui.util.fastMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.domain.model.group.GroupWithFeed
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.ext.getDefaultGroupId
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GroupWithFeedsListUseCase @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val settingsProvider: SettingsProvider,
    private val rssService: RssService,
    private val filterStateUseCase: FilterStateUseCase,
    private val diffMapHolder: DiffMapHolder,
    private val accountService: AccountService,
) {

    private var currentJob: Job? = null
    private val accountFlow = accountService.currentAccountFlow.mapNotNull { it }

    init {
        applicationScope.launch {
            accountFlow.collectLatest {
                rssService.get(it.type.id).pullFeeds().collect { feeds -> feedsFlow.value = feeds }
            }
        }
        applicationScope.launch {
            filterStateUseCase.filterStateFlow.map { it.filter }
                .combine(accountFlow) { filter, account ->
                    filter
                }.collectLatest {
                    currentJob?.cancel()
                    currentJob = when (it) {
                        Filter.Unread -> pullUnreadFeeds()
                        Filter.Starred -> pullStarredFeeds()
                        else -> pullAllFeeds()
                    }
                }
        }
    }

    private val _groupWithFeedsListFlow: MutableStateFlow<List<GroupWithFeed>> =
        MutableStateFlow<List<GroupWithFeed>>(emptyList())
    val groupWithFeedListFlow: StateFlow<List<GroupWithFeed>> = _groupWithFeedsListFlow

    private val feedsFlow: MutableStateFlow<List<GroupWithFeed>> = MutableStateFlow(emptyList())

    private val defaultGroupId get() = accountService.getCurrentAccountId().getDefaultGroupId()

    private val hideEmptyGroups get() = settingsProvider.settings.hideEmptyGroups.value

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun pullAllFeeds(): Job {
        val articleCountMapFlow =
            rssService.get().pullImportant(isStarred = false, isUnread = false)

        return applicationScope.launch {
            feedsFlow.combine(articleCountMapFlow) { groupWithFeedsList, articleCountMap ->
                groupWithFeedsList.fastFilteredMap(predicate = {
                    it.group.id != defaultGroupId || it.feeds.isNotEmpty()
                }, transform = {
                    val feedList = it.feeds.map { feed ->
                        feed.copy(important = articleCountMap[feed.id] ?: 0)
                    }
                    it.copy(feeds = feedList.toMutableList())
                })
            }.flowOn(ioDispatcher).collect { _groupWithFeedsListFlow.value = it }

        }
    }

    private fun pullStarredFeeds(): Job {
        val starredCountMap = rssService.get().pullImportant(isStarred = true, isUnread = false)

        return applicationScope.launch {

            feedsFlow.combine(starredCountMap) { groupWithFeedsList, starredCountMap ->
                val result = mutableListOf<GroupWithFeed>()
                for (groupItem in groupWithFeedsList) {

                    val feedList = groupItem.feeds.fastMap { feed ->
                        val feedCount = (starredCountMap[feed.id] ?: 0)
                        feed.copy(important = feedCount)
                    }

                    val groupItem = if (hideEmptyGroups) {
                        val filteredFeeds = feedList.filterNot { it.important == 0 }
                        if (filteredFeeds.isEmpty()) {
                            continue
                        } else {
                            groupItem.copy(feeds = filteredFeeds.toMutableList())
                        }
                    } else {
                        groupItem.copy(feeds = feedList.toMutableList())
                    }

                    if (groupItem.group.id != defaultGroupId || groupItem.feeds.isNotEmpty()) {
                        result.add(groupItem)
                    }
                }
                result
            }.flowOn(ioDispatcher).collect {
                _groupWithFeedsListFlow.value = it
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun pullUnreadFeeds(): Job {
        val unreadCountMapFlow = rssService.get().pullImportant(isStarred = false, isUnread = true)
        return applicationScope.launch {
            combine(
                feedsFlow, unreadCountMapFlow, diffMapHolder.diffMapSnapshotFlow
            ) { groupWithFeedsList, unreadCountMap, diffMap ->
                val result = mutableListOf<GroupWithFeed>()
                val unreadDiffs = diffMap.values.filter { it.isUnread }
                val readDiffs = diffMap.values.filterNot { it.isUnread }

                for (groupItem in groupWithFeedsList) {

                    val feedList = groupItem.feeds.map { feed ->
                        val feedId = feed.id
                        val feedCount = unreadCountMap[feedId] ?: 0
                        val combinedFeedCount =
                            feedCount + unreadDiffs.count { it.feedId == feedId } - readDiffs.count { it.feedId == feedId }
                        feed.copy(important = combinedFeedCount.coerceAtLeast(0))
                    }

                    val groupItem = if (hideEmptyGroups) {
                        val filteredFeeds = feedList.filterNot { it.important == 0 }
                        if (filteredFeeds.isEmpty()) {
                            continue
                        } else {
                            groupItem.copy(feeds = filteredFeeds.toMutableList())
                        }
                    } else {
                        groupItem.copy(feeds = feedList.toMutableList())
                    }

                    if (groupItem.group.id != defaultGroupId || groupItem.feeds.isNotEmpty()) {
                        result.add(groupItem)
                    }

                }
                result
            }.debounce(200L).flowOn(ioDispatcher).collect { _groupWithFeedsListFlow.value = it }
        }
    }

}