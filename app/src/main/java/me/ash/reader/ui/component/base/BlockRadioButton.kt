package me.ash.reader.ui.component.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BlockRadioButton(
    modifier: Modifier = Modifier,
    selected: Int = 0,
    onSelected: (Int) -> Unit,
    itemRadioGroups: List<BlockRadioGroupButtonItem> = listOf(),
) {

    Column {
        Row(
            modifier = modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemRadioGroups.forEachIndexed { index, item ->
                BlockButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = if (item == itemRadioGroups.last()) 0.dp else 8.dp),
                    text = item.text,
                    selected = selected == index,
                ) {
                    onSelected(index)
                    item.onClick()
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        itemRadioGroups[selected].content()
    }
}

data class BlockRadioGroupButtonItem(
    val text: String,
    val onClick: () -> Unit = {},
    val content: @Composable () -> Unit,
)