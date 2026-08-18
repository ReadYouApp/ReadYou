package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.compose.ui.text.input.KeyboardType
import org.junit.Assert.assertEquals
import org.junit.Test

class SubscribeDialogTest {
    @Test
    fun subscribeUrlKeyboardOptions_usesUriKeyboard() {
        assertEquals(KeyboardType.Uri, subscribeUrlKeyboardOptions().keyboardType)
    }
}
