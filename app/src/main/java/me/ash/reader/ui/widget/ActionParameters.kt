package me.ash.reader.ui.widget

import androidx.glance.action.ActionParameters
import androidx.glance.action.mutableActionParametersOf
import me.ash.reader.ui.page.common.ExtraName

internal fun makeActionParameters(item: Article?, dataSource: DataSource) =
    mutableActionParametersOf().apply {
        if (item != null) {
            set(ActionParameters.Key<String>(ExtraName.ARTICLE_ID), item.id)
        }
        when (dataSource) {
            is DataSource.Account -> set(ActionParameters.Key<Int>(ExtraName.ACCOUNT_ID), dataSource.accountId)
            is DataSource.Feed ->
                set(ActionParameters.Key<String>(ExtraName.FEED_ID), dataSource.feedId)
            is DataSource.Group ->
                set(ActionParameters.Key<String>(ExtraName.GROUP_ID), dataSource.groupId)
        }
    }
