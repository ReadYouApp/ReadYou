package me.ash.reader.ui.page.settings.color.reading

import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.*
import me.ash.reader.ui.component.reader.bodyStyle
import me.ash.reader.ui.component.reader.h3Style
import me.ash.reader.ui.component.reader.textHorizontalPadding

@Composable
fun TitleAndTextPreview() {
    val context = LocalContext.current
    val titleBold = LocalReadingTitleBold.current
    val subtitleBold = LocalReadingSubheadBold.current
    val titleUpperCase = LocalReadingTitleUpperCase.current
    val subheadUpperCase = LocalReadingSubheadUpperCase.current
    val titleAlign = LocalReadingTitleAlign.current
    val subtitleAlign = LocalReadingSubheadAlign.current


    val titleUpperCaseString by remember {
        derivedStateOf {
            context.getString(R.string.title).uppercase()
        }
    }

    val subheadUpperCaseString by remember {
        derivedStateOf {
            context.getString(R.string.subhead).uppercase()
        }
    }
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = textHorizontalPadding().dp),
            text = if (titleUpperCase.value) titleUpperCaseString else stringResource(id = R.string.title),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = LocalReadingFonts.current.asFontFamily(context),
                fontWeight = if (titleBold.value) FontWeight.Bold else FontWeight.Medium,
            ),
            textAlign = titleAlign.toTextAlign(),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (subheadUpperCase.value) subheadUpperCaseString else stringResource(id = R.string.subhead),
            style = h3Style().copy(textAlign = bodyStyle().textAlign),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = textHorizontalPadding().dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.run {
                merge(lineHeight = if (lineHeight.isSpecified) (lineHeight.value * LocalReadingTextLineHeight.current).sp else TextUnit.Unspecified)
            }) {
            Text(
                text = stringResource(id = R.string.preview_article_desc),
                style = bodyStyle(),
                modifier = Modifier.padding(horizontal = textHorizontalPadding().dp)
            )
        }
    }
}
