package ru.alarmneo.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StopwatchScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Секундомер (скелет)")
        Spacer(Modifier.height(8.dp))
        Text("Позже добавим круги и историю.")
    }
}
