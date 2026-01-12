package ru.alarmneo.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.viewmodel.Mode

@Composable
fun ModeToggle(
    mode: Mode,
    onModeChanged: (Mode) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { onModeChanged(Mode.Custom) }) {
            Text(if (mode == Mode.Custom) "Одиночный ✓" else "Одиночный")
        }
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = { onModeChanged(Mode.Group) }) {
            Text(if (mode == Mode.Group) "Групповой ✓" else "Групповой")
        }
    }
}
