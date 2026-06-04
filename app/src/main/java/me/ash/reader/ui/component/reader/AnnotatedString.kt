/*
 * Feeder: Android RSS reader app
 * https://gitlab.com/spacecowboy/Feeder
 *
 * Copyright (C) 2022  Jonas Kalderstam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ash.reader.ui.component.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle

class AnnotatedParagraphStringBuilder {

    // Private for a reason
    private val builder: AnnotatedString.Builder = AnnotatedString.Builder()

    private val poppedComposableStyles = mutableListOf<ComposableStyleWithStartEnd>()
    val composableStyles = mutableListOf<ComposableStyleWithStartEnd>()
    val lastTwoChars: MutableList<Char> = mutableListOf()

    val length: Int
        get() = builder.length

    val endsWithWhitespace: Boolean
        get() {
            if (lastTwoChars.isEmpty()) {
                return true
            }
            lastTwoChars.peekLatest()?.let { latest ->
                // Non-breaking space (160) is not caught by trim or whitespace identification
                if (latest.isWhitespace() || latest.code == 160) {
                    return true
                }
            }

            return false
        }

    fun pushStyle(style: SpanStyle): Int = builder.pushStyle(style = style)

    fun pushStyle(style: ParagraphStyle): Int = builder.pushStyle(style)

    fun pop(index: Int) = builder.pop(index)

    fun pop() = builder.pop()

    fun pushComposableStyle(style: @Composable () -> TextStyle): Int {
        composableStyles.add(ComposableStyleWithStartEnd(style = style, start = builder.length))
        return composableStyles.lastIndex
    }

    fun popComposableStyle(index: Int) {
        poppedComposableStyles.add(composableStyles.removeAt(index).copy(end = builder.length))
    }

    fun pushLink(link: LinkAnnotation) = builder.pushLink(link)

    fun append(text: String) {
        if (text.count() >= 2) {
            lastTwoChars.pushMaxTwo(text.secondToLast())
        }
        if (text.isNotEmpty()) {
            lastTwoChars.pushMaxTwo(text.last())
        }
        builder.append(text)
    }

    fun append(char: Char) {
        lastTwoChars.pushMaxTwo(char)
        builder.append(char)
    }

    @Composable
    fun toAnnotatedString(): AnnotatedString {
        val poppedComposableStyles = poppedComposableStyles.cleanup()
        for (composableStyle in poppedComposableStyles) {
            val style = composableStyle.style()
            val spanStyle = style.toSpanStyle()
            val paragraphStyle = style.toParagraphStyle()
            builder.addStyle(
                style = spanStyle,
                start = composableStyle.start,
                end = composableStyle.end,
            )
            builder.addStyle(
                style = paragraphStyle,
                start = composableStyle.start,
                end = composableStyle.end,
            )
        }
        val composableStyles = composableStyles.cleanup()
        for (composableStyle in composableStyles) {
            val style = composableStyle.style()
            val spanStyle = style.toSpanStyle()
            val paragraphStyle = style.toParagraphStyle()
            builder.addStyle(style = spanStyle, start = composableStyle.start, end = builder.length)
            builder.addStyle(
                style = paragraphStyle,
                start = composableStyle.start,
                end = builder.length,
            )
        }
        return builder.toAnnotatedString()
    }
}

fun AnnotatedParagraphStringBuilder.isEmpty() = lastTwoChars.isEmpty()

fun AnnotatedParagraphStringBuilder.isNotEmpty() = lastTwoChars.isNotEmpty()

fun AnnotatedParagraphStringBuilder.ensureDoubleNewline() {
    when {
        lastTwoChars.isEmpty() -> {
            // Nothing to do
        }

        length == 1 && lastTwoChars.peekLatest()?.isWhitespace() == true -> {
            // Nothing to do
        }

        length == 2 &&
            lastTwoChars.peekLatest()?.isWhitespace() == true &&
            lastTwoChars.peekSecondLatest()?.isWhitespace() == true -> {
            // Nothing to do
        }

        lastTwoChars.peekLatest() == '\n' && lastTwoChars.peekSecondLatest() == '\n' -> {
            // Nothing to do
        }

        lastTwoChars.peekLatest() == '\n' -> {
            append('\n')
        }

        else -> {
            append("\n\n")
        }
    }
}

fun AnnotatedParagraphStringBuilder.ensureSingleNewline() {
    when {
        lastTwoChars.isEmpty() -> {
            // Nothing to do
        }

        length == 1 && lastTwoChars.peekLatest()?.isWhitespace() == true -> {
            // Nothing to do
        }

        lastTwoChars.peekLatest() == '\n' -> {
            // Nothing to do
        }

        else -> {
            append('\n')
        }
    }
}

private fun CharSequence.secondToLast(): Char {
    if (count() < 2) {
        throw NoSuchElementException("List has less than two items.")
    }
    return this[lastIndex - 1]
}

private fun <T> MutableList<T>.pushMaxTwo(item: T) {
    this.add(0, item)
    if (count() > 2) {
        this.removeAt(lastIndex)
    }
}

private fun <T> List<T>.peekLatest(): T? {
    return this.firstOrNull()
}

private fun <T> List<T>.peekSecondLatest(): T? {
    if (count() < 2) {
        return null
    }
    return this[1]
}

data class ComposableStyleWithStartEnd(
    val style: @Composable () -> TextStyle,
    val start: Int,
    val end: Int = -1,
)

private fun List<ComposableStyleWithStartEnd>.cleanup(): List<ComposableStyleWithStartEnd> {
    val sortedStyles =
        this.sortedWith(
            compareBy<ComposableStyleWithStartEnd> { it.start }.thenByDescending { it.end }
        )

    val validStyles = mutableListOf<ComposableStyleWithStartEnd>()

    var lastStyleEnd = -1

    for (currentStyle in sortedStyles) {
        if (currentStyle.start >= currentStyle.end) continue
        if (currentStyle.start < lastStyleEnd) {
            val fixedStart = lastStyleEnd
            if (fixedStart < currentStyle.end) {
                validStyles.add(currentStyle.copy(start = fixedStart))
                lastStyleEnd = currentStyle.end
            }
        } else {
            validStyles.add(currentStyle)
            lastStyleEnd = currentStyle.end
        }
    }
    return validStyles
}
