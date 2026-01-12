package ru.alarmneo.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import ru.alarmneo.app.data.ThemeMode
import ru.alarmneo.app.ui.components.NeuCard
import ru.alarmneo.app.ui.components.NeuChip
import ru.alarmneo.app.ui.components.NeuTopBar
import ru.alarmneo.app.ui.components.NewToggle
import ru.alarmneo.app.ui.components.SectionHeader
import ru.alarmneo.app.ui.theme.Neu
import ru.alarmneo.app.ui.theme.neuShadow

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vibrateByDefault: Boolean,
    onVibrateByDefaultChanged: (Boolean) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit
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

                SectionHeader(title = "Внешний вид", count = null)
                Spacer(modifier = Modifier.height(16.dp))

                ThemeModeRow(
                    themeMode = themeMode,
                    onThemeModeChanged = onThemeModeChanged
                )

                Spacer(modifier = Modifier.height(18.dp))

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
private fun ThemeModeRow(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = "Тема",
            color = Neu.onBg.copy(alpha = 0.92f),
            style = MaterialTheme.typography.subtitle1
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Системная, светлая или тёмная",
            color = Neu.onBg.copy(alpha = 0.65f),
            style = MaterialTheme.typography.body2
        )

        Spacer(Modifier.height(12.dp))

        val outerShape = RoundedCornerShape(18.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(outerShape)
                .background(Neu.bg)
                .border(1.dp, Neu.outline, outerShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeSegment(
                text = "Система",
                selected = themeMode == ThemeMode.System,
                onClick = { onThemeModeChanged(ThemeMode.System) },
                modifier = Modifier.weight(1f)
            )
            ThemeSegment(
                text = "Светлая",
                selected = themeMode == ThemeMode.Light,
                onClick = { onThemeModeChanged(ThemeMode.Light) },
                modifier = Modifier.weight(1f)
            )
            ThemeSegment(
                text = "Тёмная",
                selected = themeMode == ThemeMode.Dark,
                onClick = { onThemeModeChanged(ThemeMode.Dark) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeSegment(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .then(if (selected) Modifier.neuShadow(cornerRadius = 14.dp, elevation = 7.dp) else Modifier)
            .clip(shape)
            .background(Neu.bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body2,
            color = if (selected) Neu.onBg.copy(alpha = 0.92f) else Neu.onBg.copy(alpha = 0.55f)
        )
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

        NewToggle(
            checked = toggleState,
            onCheckedChange = onToggleChange
        )
    }
}
