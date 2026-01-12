package ru.alarmneo.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import ru.alarmneo.app.ui.components.*
import ru.alarmneo.app.ui.theme.Neu



@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vibrateByDefault: Boolean,
    onVibrateByDefaultChanged: (Boolean) -> Unit
) {

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Neu.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(12.dp)
    ) {
        NeuTopBar(
            title = "Настройки",
            onNavigation = onBack,
            showSettings = false
        )


        Spacer(modifier = Modifier.height(20.dp))

        NeuCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            elevation = 6.dp,
            contentPadding = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(title = "Базовые", count = null)
                Spacer(modifier = Modifier.height(16.dp))

                RowSetting(
                    title = "Вибрация по умолчанию",
                    subtitle = "Все новые будильники будут по умолчанию с включенной вибрацией",
                    toggleState = vibrateByDefault,
                    onToggleChange = onVibrateByDefaultChanged
                )
            }
        }
    }
}

@Composable
private fun RowSetting(
    title: String,
    subtitle: String,
    toggleState: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Neu.onBg.copy(alpha = 0.92f),
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Neu.onBg.copy(alpha = 0.65f),
                style = MaterialTheme.typography.body2
            )
        }

        NewToggle(checked = toggleState, onCheckedChange = onToggleChange)
    }
}
