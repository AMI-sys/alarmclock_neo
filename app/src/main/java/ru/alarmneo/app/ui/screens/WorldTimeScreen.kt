package ru.alarmneo.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WorldTimeScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Мировое время (скелет)")
        Spacer(Modifier.height(8.dp))
        Text("Дальше добавим список городов, поиск и избранное.")
    }
}
