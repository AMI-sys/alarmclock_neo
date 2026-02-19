package ru.alarmneo.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

import ru.alarmneo.app.data.ThemeMode
import ru.alarmneo.app.ui.screens.MainScreen
import ru.alarmneo.app.ui.screens.WorldTimeScreen
import ru.alarmneo.app.ui.screens.TimerScreen
import ru.alarmneo.app.ui.screens.StopwatchScreen

@Composable
fun RootScaffold(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit
) {
    val nav = rememberNavController()
    val items = listOf(Dest.Alarm, Dest.WorldTime, Dest.Timer, Dest.Stopwatch)

    Scaffold(
        bottomBar = {
            TelegramLikeBottomBar(
                nav = nav,
                items = items
            )
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Dest.Alarm.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Dest.Alarm.route) {
                MainScreen(
                    themeMode = themeMode,
                    onThemeModeChanged = onThemeModeChanged
                )
            }
            composable(Dest.WorldTime.route) { WorldTimeScreen() }
            composable(Dest.Timer.route) { TimerScreen() }
            composable(Dest.Stopwatch.route) { StopwatchScreen() }
        }
    }
}

@Composable
private fun TelegramLikeBottomBar(
    nav: NavHostController,
    items: List<Dest>
) {
    val backStack = nav.currentBackStackEntryAsState().value
    val current = backStack?.destination?.route

    // “пилюля” + мягкий контейнер как в Telegram
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .fillMaxWidth()
    ) {
        Row(
            Modifier
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { dest ->
                val selected = current == dest.route
                val icon = when (dest) {
                    Dest.Alarm -> Icons.Default.Alarm
                    Dest.WorldTime -> Icons.Default.Public
                    Dest.Timer -> Icons.Default.Timer
                    Dest.Stopwatch -> Icons.Default.AccessTime
                }

                IconButton(
                    onClick = {
                        if (current != dest.route) {
                            nav.navigate(dest.route) {
                                popUpTo(nav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = dest.title,
                        modifier = Modifier.size(24.dp),
                        tint = if (selected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
