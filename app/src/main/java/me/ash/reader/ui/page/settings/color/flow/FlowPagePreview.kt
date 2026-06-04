package me.ash.reader.ui.page.settings.color.flow

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.R
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.infrastructure.preference.FlowArticleListTonalElevationPreference
import me.ash.reader.infrastructure.preference.FlowTopBarTonalElevationPreference
import me.ash.reader.ui.component.FilterBar
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.ext.formatAsString
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.page.home.flow.ArticleItem
import me.ash.reader.ui.theme.palette.onDark
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowPagePreview(
    topBarTonalElevation: FlowTopBarTonalElevationPreference,
    articleListTonalElevation: FlowArticleListTonalElevationPreference,
    filterBarStyle: Int,
    filterBarFilled: Boolean,
    filterBarPadding: Dp,
    filterBarTonalElevation: Dp,
) {
    var filter by remember { mutableStateOf(Filter.Unread) }

    Column(
        modifier = Modifier
            .animateContentSize()
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    articleListTonalElevation.value.dp
                ) onDark MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        val preview = generateArticleWithFeedPreview()
        val feed = preview.feed
        val article = preview.article

        TopAppBar(
            title = {
                Text(
                    text = feed.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                )
            },
            navigationIcon = {
                FeedbackIconButton(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                ) {}
            },
            actions = {
                FeedbackIconButton(
                    imageVector = Icons.Rounded.DoneAll,
                    contentDescription = stringResource(R.string.mark_all_as_read),
                    tint = MaterialTheme.colorScheme.onSurface,
                ) {}
                FeedbackIconButton(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.search),
                    tint = MaterialTheme.colorScheme.onSurface,
                ) {}
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    topBarTonalElevation.value.dp
                ),
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        ArticleItem(
            modifier = Modifier,
            feedName = feed.name,
            feedIconUrl = feed.icon,
            title = article.title,
            shortDescription = article.shortDescription,
            timeString = article.dateString,
            imgData = R.drawable.animation,
            isStarred = article.isStarred,
            isUnread = article.isUnread,
            onClick = {},
            onLongClick = null
        )

        Spacer(modifier = Modifier.height(12.dp))
        FilterBar(
            filter = filter,
            filterBarStyle = filterBarStyle,
            filterBarFilled = filterBarFilled,
            filterBarPadding = filterBarPadding,
            filterBarTonalElevation = filterBarTonalElevation,
        ) {
            filter = it
        }
    }
}

@Stable
@Composable
fun generateArticleWithFeedPreview(): ArticleWithFeed =
    ArticleWithFeed(
        Article(
            id = "",
            title = stringResource(R.string.preview_article_title),
            shortDescription = stringResource(R.string.preview_article_desc),
            rawDescription = stringResource(R.string.preview_article_desc),
            link = "",
            feedId = "",
            accountId = 0,
            date = Date(1654053729L),
            isStarred = true,
            img = null,
        ).apply {
            dateString = date.formatAsString(
                context = LocalContext.current,
                onlyHourMinute = true
            )
        },
        feed = Feed(
            id = "",
            name = stringResource(R.string.preview_feed_name),
            icon = "",
            accountId = 0,
            groupId = "",
            url = "",
        ),
    )
