package ru.alarmneo.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.ui.theme.Neu

@Composable
fun NeuTopBar(
    title: String,
    onNavigation: () -> Unit,
    showSettings: Boolean,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    NeuCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        cornerRadius = 18.dp,
        elevation = 6.dp,
        contentPadding = 14.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clickable(onClick = onNavigation),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (showSettings) Icons.Default.Settings else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (showSettings) "Settings" else "Back",
                    tint = Neu.onBg.copy(alpha = 0.80f)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                color = Neu.onBg.copy(alpha = 0.92f),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )

            if (trailingIcon != null) {
                Box(
                    modifier = Modifier.wrapContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    trailingIcon()
                }
            }
        }
    }
}

