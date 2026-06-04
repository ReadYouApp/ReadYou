package me.ash.reader.ui.component.base

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioDialog(
    modifier: Modifier = Modifier,
    visible: Boolean = false,
    title: String = "",
    description: String? = null,
    options: List<RadioDialogOption> = emptyList(),
    onDismissRequest: () -> Unit = {},
) {
    RYDialog(
        modifier = modifier,
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            LazyColumn {
                if (description != null) {
                    item {
                        Text(text = description)
                        if (options.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                items(options) { option ->
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .clip(MaterialTheme.shapes.extraLarge)
                                .selectable(selected = option.selected) {
                                    option.onClick()
                                    onDismissRequest()
                                }
                                .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = option.selected,
                            onClick = null,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                        Text(
                            modifier = Modifier.padding(start = 6.dp),
                            text = option.text,
                            style =
                                MaterialTheme.typography.bodyLarge
                                    .copy(baselineShift = BaselineShift.None)
                                    .merge(other = option.style),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
    )
}

@Immutable
data class RadioDialogOption(
    val text: String = "",
    val style: TextStyle? = null,
    val selected: Boolean = false,
    val onClick: () -> Unit = {},
)
