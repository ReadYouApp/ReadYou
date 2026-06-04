package me.ash.reader.infrastructure.android

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalDarkTheme
import me.ash.reader.infrastructure.preference.LocalOpenLink
import me.ash.reader.infrastructure.preference.LocalOpenLinkSpecificBrowser
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.ext.getCurrentVersion
import me.ash.reader.ui.ext.openURL
import me.ash.reader.ui.theme.AppTheme
import javax.inject.Inject

@AndroidEntryPoint
class CrashReportActivity : AppCompatActivity() {
    @Inject
    lateinit var settingsProvider: SettingsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val errorMessage: String = intent.getStringExtra(ERROR_REPORT_KEY).toString()

        setContent {
            settingsProvider.ProvidesSettings {
                AppTheme(useDarkTheme = LocalDarkTheme.current.isDarkTheme()) {
                    val clipboardManager = LocalClipboardManager.current
                    val appVersion = getCurrentVersion().toString()
                    val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
                    val androidVersion =
                        "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

                    val prefix =
                        "Version: $appVersion\nDevice: $deviceModel\nSystem: $androidVersion\n\n"

                    val stackTrace = "Stack trace: \n\n$errorMessage"

                    val stacktraceBlock = "Stack trace: \n\n```$errorMessage```"

                    CrashReportPage(text = prefix + stackTrace) {
                        clipboardManager.setText(AnnotatedString(prefix + stacktraceBlock))
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) finishAffinity()
    }

    companion object {
        const val ERROR_REPORT_KEY = "error_stack_trace"
    }

}


@OptIn(ExperimentalTextApi::class)
@Composable
@Preview(apiLevel = 33)
fun CrashReportPage(
    text: String = "Version: 0.9.11\n" +
            "Model: Google Pixel 6 Pro\n" + "System: Android 13 (API 33)\n\n" +
            "Stack trace: \n" +
            "\nFATAL EXCEPTION: main\n" +
            "Process: me.ash.reader, PID: 29184\n" +
            "java.lang.IllegalArgumentException: performMeasureAndLayout called during measure layout\n" +
            "\tat androidx.compose.ui.node.MeasureAndLayoutDelegate.measureAndLayout(MeasureAndLayoutDelegate.kt:133)\n" +
            "\tat androidx.compose.ui.platform.AndroidComposeView.measureAndLayout(AndroidComposeView.android.kt:34)\n" +
            "\tat androidx.compose.ui.platform.AndroidComposeView.dispatchDraw(AndroidComposeView.android.kt:15)\n" +
            "\tat android.view.View.draw(View.java:24193)\n" +
            "\tat android.view.View.updateDisplayListIfDirty(View.java:23056)\n" +
            "\tat android.view.ViewGroup.recreateChildDisplayList(ViewGroup.java:4550)\n",
    onClick: () -> Unit = {}
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        bottomBar = {}) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            Icon(
                imageVector = Icons.Outlined.BugReport,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .padding(horizontal = 16.dp)
                    .size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceTint
            )

            Text(
                text = stringResource(R.string.unexpected_error_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))


            val hyperLinkText = stringResource(R.string.submit_bug_report)
            val msg = stringResource(R.string.unexpected_error_msg).format(hyperLinkText)

            val annotatedString = buildAnnotatedString {
                append(msg.format(hyperLinkText))
                val startIndex = msg.indexOf(hyperLinkText)
                val endIndex = startIndex + hyperLinkText.length
                addLink(
                    LinkAnnotation.Url(stringResource(R.string.issue_tracer_url)),
                    start = startIndex,
                    end = endIndex
                )
            }

            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .padding(),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.copy_error_report))
                }
            }


            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(12.dp)
            ) {
                Column {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )
                }

            }

        }
    }
}
