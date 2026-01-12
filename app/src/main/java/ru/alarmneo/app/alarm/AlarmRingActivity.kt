package ru.alarmneo.app.alarm

import android.app.KeyguardManager
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.content.Context
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.CircleShape
import ru.alarmneo.app.ui.components.NewButton
import ru.alarmneo.app.ui.theme.alarmneoTheme
import ru.alarmneo.app.ui.theme.Neu
import ru.alarmneo.app.ui.theme.neuShadow

import kotlinx.coroutines.delay

import java.util.Date


class AlarmRingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(AlarmForegroundService.NOTIFICATION_ID)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //    km.requestDismissKeyguard(this, null)
        // }

        val alarmId = intent.getIntExtra(AlarmActions.EXTRA_ALARM_ID, -1)
        val label = intent.getStringExtra(AlarmActions.EXTRA_LABEL) ?: "Будильник"
        val soundId = intent.getStringExtra(AlarmActions.EXTRA_SOUND_ID) ?: "american"
        val vibrate = intent.getBooleanExtra(AlarmActions.EXTRA_VIBRATE, true)
        val snoozeMin = intent.getIntExtra(AlarmActions.EXTRA_SNOOZE_MIN, 5)
        val vibPattern =
            intent.getStringExtra(AlarmActions.EXTRA_VIBRATION_PATTERN) ?: "pulse"

        setContent {
            alarmneoTheme {
                AlarmRingScreen(
                    label = label,
                    snoozeMin = snoozeMin,
                    onDismiss = {
                        startForegroundService(AlarmForegroundServiceIntent.dismiss(this@AlarmRingActivity, alarmId))
                        finish()
                    },
                    onSnooze = {
                        startForegroundService(
                            AlarmForegroundServiceIntent.snooze(
                                this@AlarmRingActivity,
                                alarmId, label, soundId, vibrate, snoozeMin, vibPattern
                            )
                        )
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(AlarmForegroundService.NOTIFICATION_ID)
    }

    private object AlarmForegroundServiceIntent {
        fun dismiss(context: android.content.Context, alarmId: Int) =
            android.content.Intent(context, AlarmForegroundService::class.java).apply {
                action = AlarmActions.ACTION_DISMISS
                putExtra(AlarmActions.EXTRA_ALARM_ID, alarmId)
            }

        fun snooze(
            context: Context,
            alarmId: Int,
            label: String,
            soundId: String,
            vibrate: Boolean,
            snoozeMin: Int,
            vibPattern: String
        ) =
            android.content.Intent(context, AlarmForegroundService::class.java).apply {
                action = AlarmActions.ACTION_SNOOZE
                putExtra(AlarmActions.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmActions.EXTRA_LABEL, label)
                putExtra(AlarmActions.EXTRA_SOUND_ID, soundId)
                putExtra(AlarmActions.EXTRA_VIBRATE, vibrate)
                putExtra(AlarmActions.EXTRA_VIBRATION_PATTERN, vibPattern)
                putExtra(AlarmActions.EXTRA_SNOOZE_MIN, snoozeMin)
            }
    }
}

@Composable
private fun AlarmRingScreen(
    label: String,
    snoozeMin: Int,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val context = LocalContext.current

    // Текущее время: обновляем раз в секунду
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val timeText = remember(now) {
        DateFormat.getTimeFormat(context).format(Date(now))
    }

    // Пульсация центрального элемента
    val inf = rememberInfiniteTransition(label = "ringPulse")
    val pulse by inf.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by inf.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neu.bg)
            .padding(20.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Верх: текущее время + подпись
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.h3,
                color = Neu.onBg.copy(alpha = 0.95f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label.ifBlank { "Будильник" },
                style = MaterialTheme.typography.h6,
                color = Neu.onBg.copy(alpha = 0.78f)
            )
        }

        // Центр: “будильник” с мягкой пульсацией
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(210.dp),
            contentAlignment = Alignment.Center
        ) {
            // внешнее “кольцо”
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .scale(pulse)
                    .alpha(pulseAlpha)
                    .neuShadow(cornerRadius = 999.dp, elevation = 14.dp)
                    .clip(CircleShape)
                    .background(Neu.bg)
                    .border(1.dp, Neu.outline.copy(alpha = 0.35f), CircleShape)
            )

            // внутренний круг
            Box(
                modifier = Modifier
                    .size(138.dp)
                    .neuShadow(cornerRadius = 999.dp, elevation = 10.dp)
                    .clip(CircleShape)
                    .background(Neu.bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessAlarm,
                    contentDescription = null,
                    tint = Neu.onBg.copy(alpha = 0.92f),
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        // Низ: две большие круглые кнопки
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeuActionCircle(
                title = "Отложить",
                subtitle = "ещё $snoozeMin мин",
                icon = Icons.Filled.Schedule,
                accent = MaterialTheme.colors.primary.copy(alpha = 0.95f),
                onClick = onSnooze
            )

            NeuActionCircle(
                title = "Выключить",
                subtitle = "остановить",
                icon = Icons.Filled.Close,
                accent = Neu.onBg.copy(alpha = 0.92f),
                onClick = onDismiss
            )

        }
    }
}

@Composable
private fun NeuActionCircle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .neuShadow(cornerRadius = 999.dp, elevation = 12.dp)
                .clip(CircleShape)
                .background(Neu.bg)
                .border(1.dp, Neu.outline.copy(alpha = 0.35f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.subtitle1, color = Neu.onBg.copy(alpha = 0.90f))
        Text(subtitle, style = MaterialTheme.typography.caption, color = Neu.onBg.copy(alpha = 0.65f))
    }
}

