package me.ash.reader.infrastructure.compose

import androidx.compose.runtime.Composable

@Composable
fun ProvideCompositionLocals(content: @Composable () -> Unit) {
    ProvideUriHandler(content)
}
