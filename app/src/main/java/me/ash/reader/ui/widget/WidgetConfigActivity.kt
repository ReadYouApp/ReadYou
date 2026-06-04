package me.ash.reader.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalDarkTheme
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.theme.AppTheme

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    @Inject lateinit var repository: WidgetRepository
    @Inject lateinit var settingsProvider: SettingsProvider
    @Inject lateinit var workManager: WorkManager
    private val glanceManager by lazy { GlanceAppWidgetManager(this) }
    private val appWidgetManager by lazy { AppWidgetManager.getInstance(this) }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val glanceId = glanceManager.getGlanceIdBy(intent)

        if (glanceId == null) {
            finish()
            return
        }

        val appWidgetId = glanceManager.getAppWidgetId(glanceId)

        val cardIds = runBlocking { glanceManager.getGlanceIds(ArticleCardWidget::class.java) }

        val isCard = cardIds.contains(glanceId)

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        enableEdgeToEdge()

        val config = runBlocking { repository.getConfig(appWidgetId) }

        setContent {
            settingsProvider.ProvidesSettings {
                AppTheme(useDarkTheme = LocalDarkTheme.current.isDarkTheme()) {
                    var config: WidgetConfig by remember { mutableStateOf(config) }

                    val dataSources =
                        repository.getCurrentDataSources().collectAsStateValue(emptyList())

                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxSize(),
                        floatingActionButton = {
                            MediumFloatingActionButton(
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.loweredElevation(),
                                containerColor = MaterialTheme.colorScheme.primaryFixed,
                                contentColor = MaterialTheme.colorScheme.onPrimaryFixedVariant,
                                onClick = { updateWidget(config, glanceId, appWidgetId, isCard) },
                            ) {
                                Icon(
                                    Icons.Rounded.Done,
                                    stringResource(R.string.done),
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                        },
                        floatingActionButtonPosition = FabPosition.Center,
                    ) { innerPadding ->
                        Column(modifier = Modifier.padding()) {
                            LazyColumn(contentPadding = innerPadding) {
                                item {
                                    Text(
                                        modifier =
                                            Modifier.padding(
                                                top = 64.dp,
                                                start = 24.dp,
                                                bottom = 24.dp,
                                                end = 24.dp,
                                            ),
                                        text = stringResource(R.string.widget_settings),
                                        style =
                                            MaterialTheme.typography.displaySmall.merge(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily =
                                                    when (config.theme) {
                                                        Theme.SansSerif -> FontFamily.SansSerif
                                                        Theme.Serif -> FontFamily.Serif
                                                    },
                                            ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                if (!isCard) {
                                    item { Subtitle(stringResource(R.string.theme)) }
                                    item {
                                        ThemePicker(
                                            modifier = Modifier.fillMaxWidth(),
                                            selected = config.theme,
                                        ) {
                                            config = config.copy(theme = it)
                                        }
                                    }
                                }
                                item { Subtitle(text = stringResource(R.string.data_source)) }

                                items(dataSources) { (name, dataSource) ->
                                    SingleChoiceItem(
                                        selected = config.dataSource == dataSource,
                                        onClick = { config = config.copy(dataSource = dataSource) },
                                        title = name,
                                        description =
                                            when (dataSource) {
                                                is DataSource.Account ->
                                                    stringResource(R.string.account)
                                                is DataSource.Feed -> stringResource(R.string.feed)
                                                is DataSource.Group ->
                                                    stringResource(R.string.folder)
                                            },
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(120.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateWidget(
        config: WidgetConfig,
        glanceId: GlanceId,
        appWidgetId: Int,
        isCard: Boolean,
    ) {
        lifecycleScope
            .launch {
                repository.writeConfig(appWidgetId, config)
                if (isCard) {
                    ArticleCardWidget().update(this@WidgetConfigActivity, id = glanceId)
                } else {
                    ArticleListWidget().update(this@WidgetConfigActivity, id = glanceId)
                }
            }
            .invokeOnCompletion {
                val resultValue =
                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(RESULT_OK, resultValue)
                finish()
            }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Subtitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.padding(top = 20.dp, bottom = 8.dp).padding(horizontal = 24.dp),
        style = MaterialTheme.typography.labelLargeEmphasized,
        color = MaterialTheme.colorScheme.primary,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemePicker(modifier: Modifier = Modifier, selected: Theme?, onClick: (Theme) -> Unit) {
    val interactionSource1 = remember { MutableInteractionSource() }
    val interactionSource2 = remember { MutableInteractionSource() }

    ButtonGroup(overflowIndicator = {}, modifier = modifier.padding(horizontal = 12.dp)) {
        customItem(
            buttonGroupContent = {
                ToggleButton(
                    checked = selected == Theme.SansSerif,
                    onCheckedChange = { onClick(Theme.SansSerif) },
                    interactionSource = interactionSource1,
                    modifier = Modifier.animateWidth(interactionSource1).weight(1f),
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 4.dp),
                        text = "Sans Serif",
                        style = LocalTextStyle.current.merge(fontFamily = FontFamily.SansSerif),
                    )
                }
            }
        ) {}
        customItem(
            buttonGroupContent = {
                ToggleButton(
                    checked = selected == Theme.Serif,
                    onCheckedChange = { onClick(Theme.Serif) },
                    interactionSource = interactionSource2,
                    modifier = Modifier.animateWidth(interactionSource2).weight(1f),
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 4.dp),
                        text = "Serif",
                        style =
                            LocalTextStyle.current.merge(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }
            }
        ) {}
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SingleChoiceItem(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    interactionSource = interactionSource,
                )
                .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLargeEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RadioButton(selected = selected, onClick = null, interactionSource = interactionSource)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
private fun Preview() {
    Surface {
        Column {
            var selected by remember { mutableStateOf(Theme.SansSerif) }
            Subtitle("Theme")
            ThemePicker(
                modifier = Modifier.fillMaxWidth(),
                selected = selected,
                onClick = { selected = it },
            )
            Subtitle("Data source")
            SingleChoiceItem(
                selected = false,
                title = "Read You",
                description = "Account",
                onClick = {},
            )
            SingleChoiceItem(
                selected = true,
                title = "Android",
                description = "Group",
                onClick = {},
            )
        }
    }
}
