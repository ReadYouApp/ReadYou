package me.ash.reader.ui.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Dimension
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.ash.reader.R
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.domain.service.AccountService
import me.ash.reader.infrastructure.di.ApplicationScope

internal val Context.widgetDataStore by preferencesDataStore("widgets")

@Singleton
class WidgetRepository
@Inject
constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val groupDao: GroupDao,
    private val accountService: AccountService,
) {

    private val json = Json { ignoreUnknownKeys = true }

    private fun Context.getConfigFlow(
        widgetId: Int,
        default: WidgetConfig = WidgetConfig.default(accountService.getCurrentAccountId()),
    ): Flow<WidgetConfig> =
        widgetDataStore.data.map {
            val string = it[stringPreferencesKey(widgetId.toString())]
            if (string == null) default
            else
                runCatching { json.decodeFromString<WidgetConfig>(string) }
                    .getOrDefault(default)
        }

    private suspend fun Context.writeConfig(widgetId: Int, config: WidgetConfig) =
        withContext(Dispatchers.IO) {
            widgetDataStore.edit { preferences ->
                val configString =
                    runCatching { json.encodeToString(config) }.getOrNull() ?: return@edit
                preferences[stringPreferencesKey(widgetId.toString())] = configString
            }
        }

    fun getCurrentDataSources(): Flow<List<NamedDataSource>> =
        accountService.currentAccountFlow.mapNotNull { account ->
            val accountId = account?.id!!
            val feeds = feedDao.queryAll(accountId)
            val groups = groupDao.queryAll(accountId)
            buildList {
                add(NamedDataSource(name = account.name, DataSource.Account(accountId)))
                groups.mapTo(this) {
                    NamedDataSource(name = it.name, dataSource = DataSource.Group(it.id))
                }
                feeds.mapTo(this) {
                    NamedDataSource(name = it.name, dataSource = DataSource.Feed(it.id))
                }
            }
        }

    fun getDefaultConfig() = WidgetConfig.default(accountService.getCurrentAccountId())

    suspend fun writeConfig(widgetId: Int, config: WidgetConfig) =
        context.writeConfig(widgetId, config)

    suspend fun getConfig(widgetId: Int): WidgetConfig = getConfigFlow(widgetId).first()

    fun getConfigFlow(widgetId: Int): Flow<WidgetConfig> = context.getConfigFlow(widgetId)

    fun clearConfig(widgetIds: IntArray) =
        coroutineScope.launch(Dispatchers.IO) {
            context.widgetDataStore.edit { preferences ->
                widgetIds.forEach { preferences.remove(stringPreferencesKey(it.toString())) }
            }
        }

    private fun getArticles(dataSource: DataSource): Flow<List<Article>> =
        when (dataSource) {
            is DataSource.Account ->
                articleDao.queryLatestUnreadArticles(accountId = dataSource.accountId)
            is DataSource.Feed ->
                articleDao.queryLatestUnreadArticlesFromFeed(feedId = dataSource.feedId)
            is DataSource.Group ->
                articleDao.queryLatestUnreadArticlesFromGroup(groupId = dataSource.groupId)
        }.map { items ->
            items.map { (article, feed) ->
                Article(
                    title = article.title,
                    imgUrl = article.img,
                    feedName = feed.name,
                    id = article.id,
                )
            }
        }

    fun getData(dataSource: DataSource): Flow<WidgetData> {
        return getArticles(dataSource).map { articles ->
            val title =
                when (dataSource) {
                    is DataSource.Account -> context.getString(R.string.unread)
                    is DataSource.Feed ->
                        feedDao.queryById(dataSource.feedId)?.name
                            ?: context.getString(R.string.unread)
                    is DataSource.Group ->
                        groupDao.queryById(dataSource.groupId)?.name
                            ?: context.getString(R.string.unread)
                }
            WidgetData(title, articles)
        }
    }

    suspend fun fetchBitmap(imgUrl: String?): Bitmap? {
        if (imgUrl == null) return null
        return withContext(Dispatchers.IO) {
            val link = imgUrl
            val imageLoader = context.imageLoader
            imageLoader
                .execute(
                    ImageRequest.Builder(context)
                        .data(link)
                        .size(width = Dimension.Pixels(600), height = Dimension.Undefined)
                        .build()
                )
                .drawable
                ?.toBitmapOrNull()
        }
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface WidgetEntryPoint {
        fun repository(): WidgetRepository
    }

    companion object {
        fun get(context: Context): WidgetRepository {
            return EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java,
                )
                .repository()
        }
    }
}

data class NamedDataSource(val name: String, val dataSource: DataSource)

data class WidgetData(val title: String, val articles: List<Article>)

data class Article(
    val id: String,
    val title: String,
    val imgUrl: String? = null,
    val feedName: String,
)
