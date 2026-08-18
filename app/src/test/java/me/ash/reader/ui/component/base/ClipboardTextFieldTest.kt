package me.ash.reader.ui.component.base

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import org.junit.Assert.assertEquals
import org.junit.Test

class ClipboardTextFieldTest {
    @Test
    fun withImeAction_preservesKeyboardType() {
        val options =
            KeyboardOptions(keyboardType = KeyboardType.Uri).withImeAction(ImeAction.Search)

        assertEquals(KeyboardType.Uri, options.keyboardType)
        assertEquals(ImeAction.Search, options.imeAction)
    }
}
