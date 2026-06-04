package me.ash.reader.domain.service

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.infrastructure.di.ApplicationScope

class RssService
@Inject
constructor(
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val accountService: AccountService,
    private val localRssService: LocalRssService,
    private val feverRssService: FeverRssService,
    private val googleReaderRssService: GoogleReaderRssService,
) {

    private val currentServiceFlow =
        accountService.currentAccountFlow
            .mapNotNull { it }
            .map { it.type.id }
            .distinctUntilChanged()
            .map { get(it) }
            .stateIn(coroutineScope, SharingStarted.Eagerly, localRssService)

    fun get() = get(accountService.getCurrentAccount().type.id)

    fun flow() = currentServiceFlow

    fun get(accountTypeId: Int) =
        when (accountTypeId) {
            AccountType.Local.id -> localRssService
            AccountType.Fever.id -> feverRssService
            AccountType.GoogleReader.id -> googleReaderRssService
            AccountType.FreshRSS.id -> googleReaderRssService
            AccountType.Inoreader.id -> localRssService
            AccountType.Feedly.id -> localRssService
            else -> localRssService
        }
}
