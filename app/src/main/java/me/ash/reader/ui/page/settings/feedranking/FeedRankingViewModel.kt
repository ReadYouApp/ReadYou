package me.ash.reader.ui.page.settings.feedranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.service.AccountService

@HiltViewModel
class FeedRankingViewModel @Inject constructor(
    private val feedDao: FeedDao,
    private val accountService: AccountService,
) : ViewModel() {

    private val _feeds = MutableStateFlow<List<Feed>>(emptyList())
    val feeds: StateFlow<List<Feed>> = _feeds.asStateFlow()

    private val _persistQueue = MutableSharedFlow<List<Feed>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        viewModelScope.launch {
            accountService.currentAccountIdFlow
                .filterNotNull()
                .collect { accountId ->
                    _feeds.value = feedDao.queryAllSortedByRank(accountId)
                }
        }
        viewModelScope.launch {
            _persistQueue
                .debounce(300)
                .collect { feeds ->
                    feeds.forEachIndexed { index, feed ->
                        feedDao.updateRank(feed.id, index + 1)
                    }
                }
        }
    }

    fun reorder(from: Int, to: Int) {
        val current = _feeds.value
        if (from < 0 || to < 0 || from >= current.size || to >= current.size) return
        val reordered = current.toMutableList().apply { add(to, removeAt(from)) }
        _feeds.value = reordered
        _persistQueue.tryEmit(reordered)
    }
}
