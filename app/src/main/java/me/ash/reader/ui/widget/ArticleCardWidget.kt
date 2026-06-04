package me.ash.reader.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.ash.reader.R
import me.ash.reader.infrastructure.android.MainActivity
import me.ash.reader.ui.ext.collectAsStateValue
import timber.log.Timber

@AndroidEntryPoint
class ArticleCardWidgetReceiver : GlanceAppWidgetReceiver() {
    @Inject lateinit var repository: WidgetRepository
    override val glanceAppWidget: GlanceAppWidget = ArticleCardWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        repository.clearConfig(appWidgetIds)
    }
}

class ArticleCardWidget() : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        runCatching {
                val repository = WidgetRepository.get(context)
                val config = repository.getDefaultConfig()
                val data =
                    withContext(Dispatchers.IO) { repository.getData(config.dataSource).first() }
                val article =
                    data.articles.let {
                        it.firstOrNull { !it.imgUrl.isNullOrEmpty() } ?: it.firstOrNull()
                    }
                val bitmap = repository.fetchBitmap(article?.imgUrl)

                provideContent {
                    GlanceTheme {
                        GlanceTheme {
                            ArticleCard(
                                article = article,
                                dataSource = config.dataSource,
                                bitmap = bitmap,
                            )
                        }
                    }
                }
            }
            .onFailure { Timber.e(it) }
    }

    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = WidgetRepository.get(context)
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        val initialConfig = repository.getConfig(widgetId)

        val initialData =
            withContext(Dispatchers.IO) { repository.getData(initialConfig.dataSource).first() }

        val configFlow = repository.getConfigFlow(widgetId)

        val initialArticle =
            withContext(Dispatchers.IO) {
                initialData.articles.let {
                    it.firstOrNull { !it.imgUrl.isNullOrEmpty() } ?: it.firstOrNull()
                }
            }

        val initialBitmap = repository.fetchBitmap(initialArticle?.imgUrl)

        provideContent {
            val (_, dataSource) = configFlow.collectAsStateValue(initialConfig)

            val articleFlow =
                remember(dataSource) { repository.getData(dataSource) }
                    .map {
                        it.articles.let {
                            it.firstOrNull { !it.imgUrl.isNullOrEmpty() } ?: it.firstOrNull()
                        }
                    }

            val article = articleFlow.collectAsStateValue(initialArticle)

            val bitmap =
                articleFlow
                    .map { repository.fetchBitmap(it?.imgUrl) }
                    .collectAsStateValue(initialBitmap)

            GlanceTheme { ArticleCard(article = article, dataSource = dataSource, bitmap = bitmap) }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun ArticleCard(
    article: Article?,
    bitmap: Bitmap?,
    dataSource: DataSource,
    modifier: GlanceModifier = GlanceModifier,
) {

    val context = LocalContext.current

    val titleColor =
        if (bitmap != null) Color.White else GlanceTheme.colors.onSurface.getColor(context)

    val feedColor =
        if (bitmap != null) Color.White else GlanceTheme.colors.primary.getColor(context)

    val parameters = makeActionParameters(article, dataSource)

    // create your AppWidget here
    WidgetContainer(modifier = modifier) {
        Box(
            contentAlignment = Alignment.BottomStart,
            modifier =
                GlanceModifier.fillMaxSize()
                    .clickable(actionStartActivity<MainActivity>(parameters)),
        ) {
            if (bitmap != null) {
                Spacer(
                    modifier =
                        GlanceModifier.fillMaxSize()
                            .background(
                                imageProvider = ImageProvider(bitmap),
                                contentScale = ContentScale.Crop,
                            )
                )
                Spacer(
                    modifier =
                        GlanceModifier.fillMaxSize()
                            .background(imageProvider = ImageProvider(R.drawable.scrim_gradient))
                )
            }

            Column(GlanceModifier.padding(12.dp)) {
                if (article == null) {
                    Text(
                        context.getString(R.string.no_unread_articles),
                        style =
                            TextStyle(
                                color = ColorProvider(feedColor),
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        modifier = GlanceModifier.padding(bottom = 8.dp, start = 4.dp),
                    )
                } else {
                    Text(
                        article.feedName,
                        style =
                            TextStyle(
                                color = ColorProvider(feedColor),
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        maxLines = 1,
                    )
                    Text(
                        article.title,
                        style =
                            TextStyle(
                                color = ColorProvider(titleColor),
                                fontFamily = FontFamily.Serif,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        maxLines = 6,
                    )
                }
            }
        }
    }
}
