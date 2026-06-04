package me.ash.reader.ui.component.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BoldCharactersIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current,
    activated: Boolean = false,
) {

    val baseStyle = LocalTextStyle.current.merge(
        fontFamily = FontFamily.SansSerif,
        fontSize = (size.value * 0.8F).sp,
        color = tint,
        textDecoration = if (activated) TextDecoration.Underline else TextDecoration.None
    ).toSpanStyle()

    val string = remember(baseStyle, activated) {
        buildAnnotatedString {
            pushStyle(
                baseStyle.copy(
                    fontWeight = if (activated) FontWeight.W900 else FontWeight.Medium,
                    textDecoration = if (activated) TextDecoration.Underline else TextDecoration.None
                )
            )
            append("B")
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Text(string)
    }
}

@Preview(backgroundColor = 0xFFFFFF)
@Composable
private fun BoldCharactersIconPreview() {
    Column {
        BoldCharactersIcon()
        BoldCharactersIcon(activated = true)
    }
}
