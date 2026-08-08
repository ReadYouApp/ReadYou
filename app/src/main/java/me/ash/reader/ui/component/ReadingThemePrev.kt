package me.ash.reader.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.ash.reader.infrastructure.preference.LocalReadingImageHorizontalPadding
import me.ash.reader.infrastructure.preference.LocalReadingImageRoundedCorners
import me.ash.reader.infrastructure.preference.LocalReadingTextAlign
import me.ash.reader.infrastructure.preference.ReadingThemePreference
import me.ash.reader.ui.theme.Shape24
import me.ash.reader.ui.theme.palette.onDark
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun ReadingThemePrev(
    selected: ReadingThemePreference = ReadingThemePreference.MaterialYou,
    theme: ReadingThemePreference = ReadingThemePreference.MaterialYou,
    onClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val imageRoundedCorners = LocalReadingImageRoundedCorners.current
    val roundedCorners by remember { mutableStateOf(RoundedCornerShape((imageRoundedCorners / 2).dp)) }

    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(Shape24)
            .background(MaterialTheme.colorScheme.inverseOnSurface
                    onLight MaterialTheme.colorScheme.surface
            )
            .border(
                width = animateDpAsState(if (selected == theme) 4.dp else (-1).dp).value,
                color = MaterialTheme.colorScheme.primary,
                shape = Shape24
            )
            .clickable(onClick = onClick),
        horizontalAlignment = when (theme) {
            ReadingThemePreference.MaterialYou -> Alignment.Start
            ReadingThemePreference.Reeder -> Alignment.Start
            ReadingThemePreference.Paper -> Alignment.CenterHorizontally
            ReadingThemePreference.Custom -> LocalReadingTextAlign.current.toAlignment()
        }
    ) {
        Spacer(modifier = Modifier.height(22.dp))
        // Header
        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            text = theme.toDesc(context),
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(modifier = Modifier.height(2.dp))
        // Metadata
        Box(modifier = Modifier
            .padding(horizontal = 12.dp)
            .size(width = 32.dp, height = 4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Paragraph
        Box(modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .height(12.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier
            .padding(horizontal = 12.dp)
            .width(114.dp)
            .height(12.dp)
        ) {
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Box(modifier = Modifier
                .padding(start = 4.dp)
                .weight(2f)
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Image
        Box(modifier = Modifier
            .padding(horizontal = when (theme) {
                ReadingThemePreference.MaterialYou -> 12.dp
                ReadingThemePreference.Reeder -> 0.dp
                ReadingThemePreference.Paper -> 12.dp
                ReadingThemePreference.Custom -> (LocalReadingImageHorizontalPadding.current / 2).dp
            })
            .fillMaxWidth()
            .height(46.dp)
            .clip(when (theme) {
                ReadingThemePreference.MaterialYou -> MaterialTheme.shapes.medium
                ReadingThemePreference.Reeder -> RectangleShape
                ReadingThemePreference.Paper -> RectangleShape
                ReadingThemePreference.Custom -> roundedCorners
            })
            .background(MaterialTheme.colorScheme.primaryContainer onDark MaterialTheme.colorScheme.secondaryContainer)
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Footer
        Box(modifier = Modifier
            .padding(horizontal = 12.dp)
            .width(100.dp)
            .height(12.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Preview
@Composable
private fun ReadYouPreview() {
    ReadingThemePrev(
        selected = ReadingThemePreference.MaterialYou,
        theme = ReadingThemePreference.MaterialYou,
    )
}

@Preview
@Composable
private fun ReederPreview() {
    ReadingThemePrev(
        theme = ReadingThemePreference.Reeder,
    )
}

@Preview
@Composable
private fun PaperPreview() {
    ReadingThemePrev(
        theme = ReadingThemePreference.Paper,
    )
}

@Preview
@Composable
private fun CustomPreview() {
    ReadingThemePrev(
        theme = ReadingThemePreference.Custom,
    )
}
