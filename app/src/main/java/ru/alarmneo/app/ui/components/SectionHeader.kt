package ru.alarmneo.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.ui.theme.Neu

@Composable
fun SectionHeader(
    title: String,
    count: Int? = null,
    modifier: Modifier = Modifier
) {
    NeuCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        cornerRadius = 14.dp,
        elevation = 6.dp,
        contentPadding = 12.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle2,
                color = Neu.onBg.copy(alpha = 0.88f),
                modifier = Modifier.weight(1f)
            )
            if (count != null) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.caption,
                    color = Neu.onBg.copy(alpha = 0.55f)
                )
            }
        }
    }
}
