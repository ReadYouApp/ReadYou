package me.ash.reader.ui.widget

import kotlinx.serialization.Serializable

@Serializable
data class WidgetConfig(val theme: Theme, val dataSource: DataSource) {
    companion object {
        fun default(accountId: Int) =
            WidgetConfig(theme = Theme.SansSerif, dataSource = DataSource.Account(accountId))
    }
}

@Serializable
sealed interface DataSource {
    @Serializable data class Feed(val feedId: String) : DataSource

    @Serializable data class Group(val groupId: String) : DataSource

    @Serializable data class Account(val accountId: Int) : DataSource
}

@Serializable
enum class Theme {
    SansSerif,
    Serif,
}
