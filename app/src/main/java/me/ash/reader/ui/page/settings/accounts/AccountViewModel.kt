package me.ash.reader.ui.page.settings.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.OpmlService
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.DefaultDispatcher
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.di.MainDispatcher
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountService: AccountService,
    private val rssService: RssService,
    private val opmlService: OpmlService,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    @MainDispatcher
    private val mainDispatcher: CoroutineDispatcher,
    @ApplicationScope
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    private val _accountUiState = MutableStateFlow(AccountUiState())
    val accountUiState: StateFlow<AccountUiState> = _accountUiState.asStateFlow()
    val accounts = accountService.getAccounts()
    var addAccountJob: Job? = null

    fun initData(accountId: Int) {
        viewModelScope.launch(ioDispatcher) {
            _accountUiState.update { it.copy(selectedAccount = accountService.getAccountFlowById(accountId)) }
        }
    }

    fun update(accountId: Int, block: Account.() -> Account) {
        applicationScope.launch(ioDispatcher) {
            accountService.update(accountId, block)
            rssService.get(accountId).clearAuthorization()
            // 同步设置（间隔/仅 WiFi/仅充电）变更后立即重排周期任务，无需重启应用
            if (accountId == accountService.getCurrentAccountId()) {
                accountService.getAccountById(accountId)?.let {
                    rssService.get(it.type.id).reschedulePeriodicWork(it)
                }
            }
        }
    }

    fun exportAsOPML(accountId: Int, callback: (String) -> Unit = {}) {
        viewModelScope.launch(defaultDispatcher) {
            callback(opmlService.saveToString(accountId,
                _accountUiState.value.exportOPMLMode == ExportOPMLMode.ATTACH_INFO))
        }
    }

    fun hideDeleteDialog() {
        _accountUiState.update { it.copy(deleteDialogVisible = false) }
    }

    fun showDeleteDialog() {
        _accountUiState.update { it.copy(deleteDialogVisible = true) }
    }

    fun showClearDialog() {
        _accountUiState.update { it.copy(clearDialogVisible = true) }
    }

    fun hideClearDialog() {
        _accountUiState.update { it.copy(clearDialogVisible = false) }
    }

    fun delete(accountId: Int, callback: () -> Unit = {}) {
        viewModelScope.launch(ioDispatcher) {
            accountService.delete(accountId)
            // 删除账户后，为目标账户重建周期同步任务
            rssService.get().reschedulePeriodicWork()
            withContext(mainDispatcher) {
                callback()
            }
        }
    }

    fun clear(account: Account, callback: () -> Unit = {}) {
        viewModelScope.launch(ioDispatcher) {
            rssService.get(account.type.id).deleteAccountArticles(account.id!!)
            withContext(mainDispatcher) {
                callback()
            }
        }
    }

    fun addAccount(account: Account, callback: (account: Account?, exception: Exception?) -> Unit) {
        setLoading(true)
        addAccountJob = applicationScope.launch(ioDispatcher) {
            val addAccount = accountService.addAccount(account)
            try {
                val rssRepo = rssService.get(addAccount.type.id)
                if (rssRepo.validCredentials(account)) {
                    rssRepo.doSyncOneTime()
                    rssRepo.initSync()
                    withContext(mainDispatcher) {
                        callback(addAccount, null)
                    }
                } else {
                    throw Exception("Unauthorized")
                }
            } catch (e: Exception) {
                accountService.delete(addAccount.id!!)
                withContext(mainDispatcher) {
                    callback(null, e)
                }
            } finally {
                setLoading(false)
            }
        }
    }

    fun switchAccount(targetAccount: Account, callback: () -> Unit = {}) {
        viewModelScope.launch(ioDispatcher) {
            accountService.switch(targetAccount)
            // 切换账户后立即以新账户的设置重排周期任务，
            // 避免周期任务继续携带旧账户的 accountId 运行
            rssService.get(targetAccount.type.id).reschedulePeriodicWork(targetAccount)
            withContext(mainDispatcher) {
                callback()
            }
        }
    }

    fun changeExportOPMLMode(mode: ExportOPMLMode) {
        viewModelScope.launch {
            _accountUiState.update {
                it.copy(
                    exportOPMLMode = mode
                )
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        viewModelScope.launch {
            _accountUiState.update {
                it.copy(
                    isLoading = isLoading
                )
            }
        }
    }
    
    fun cancelAdd() {
        addAccountJob?.cancel()
        setLoading(false)
    }
}

data class AccountUiState(
    val selectedAccount: Flow<Account?> = emptyFlow(),
    val deleteDialogVisible: Boolean = false,
    val clearDialogVisible: Boolean = false,
    val exportOPMLMode: ExportOPMLMode = ExportOPMLMode.ATTACH_INFO,
    val isLoading: Boolean = false,
)

sealed class ExportOPMLMode {
    object ATTACH_INFO : ExportOPMLMode()
    object NO_ATTACH : ExportOPMLMode()
}
