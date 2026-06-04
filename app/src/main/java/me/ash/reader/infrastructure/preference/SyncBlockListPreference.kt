package me.ash.reader.infrastructure.preference

import me.ash.reader.ui.page.settings.accounts.AccountViewModel

typealias SyncBlockList = List<String>

object SyncBlockListPreference {

    val default: SyncBlockList = emptyList()

    fun put(accountId: Int, viewModel: AccountViewModel, syncBlockList: SyncBlockList) {
        viewModel.update(accountId) { copy(syncBlockList = syncBlockList) }
    }

    fun of(syncBlockList: String): SyncBlockList {
        return syncBlockList.split("\n")
    }

    fun toString(syncBlockList: SyncBlockList): String = syncBlockList
        .filter { it.isNotBlank() }
        .map { it.trim() }
        .joinToString { "$it\n" }
}
