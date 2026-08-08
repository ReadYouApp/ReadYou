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

import android.content.Context
import androidx.compose.foundation.lazy.LazyListScope
import me.ash.reader.R

@Suppress("FunctionName")
fun LazyListScope.Reader(
    context: Context,
    subheadUpperCase: Boolean = false,
    link: String,
    content: String,
    onImageClick: ((imgUrl: String, altText: String) -> Unit)? = null,
    onLinkClick: (String) -> Unit
) {
//    Log.i("RLog", "Reader: ")
    htmlFormattedText(
        inputStream = content.byteInputStream(),
        subheadUpperCase = subheadUpperCase,
        baseUrl = link,
        onImageClick = onImageClick,
        imagePlaceholder = R.drawable.ic_launcher_foreground,
        onLinkClick = onLinkClick
    )
}
