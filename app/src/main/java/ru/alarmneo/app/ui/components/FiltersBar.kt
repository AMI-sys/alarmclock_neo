package ru.alarmneo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.model.WeekDay
import ru.alarmneo.app.ui.theme.AccentWarm
import ru.alarmneo.app.ui.theme.BlueMuted
import ru.alarmneo.app.ui.theme.Neu

@Composable
fun FiltersBar(
    selectedDay: WeekDay?,
    selectedGroup: String?,
    groups: List<String>,
    onDayChanged: (WeekDay?) -> Unit,
    onGroupChanged: (String?) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var groupMenu by remember { mutableStateOf(false) }

    val hasActiveFilters = (selectedDay != null) || (selectedGroup != null)

    NeuCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        cornerRadius = 20.dp,
        elevation = 10.dp,
        backgroundColor = Neu.bg,
        outlineColor = AccentWarm.copy(alpha = 0.40f),
        contentPadding = 12.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Тонкий тёплый акцент (вместо “оранжевого ведра”)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(AccentWarm.copy(alpha = 0.70f))
            )

            // Header row: "Фильтры" + "Сброс"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Фильтры",
                    style = MaterialTheme.typography.caption,
                    color = Neu.onBg.copy(alpha = 0.85f)
                )

                if (hasActiveFilters) {
                    NeuChip(
                        text = "Сброс",
                        selected = false,
                        onClick = onReset,
                        modifier = Modifier
                    )
                }
            }

            // Days
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                item {
                    NeuChip(
                        text = "Все",
                        selected = selectedDay == null,
                        onClick = { onDayChanged(null) }
                    )
                }

                items(weekDaysOrdered()) { d ->
                    NeuChip(
                        text = shortName(d),
                        selected = selectedDay == d,
                        onClick = { onDayChanged(d) }
                    )
                }
            }

            // Group selector (chip + dropdown)
            Box {
                val groupLabel = selectedGroup?.let { "Группа: $it" } ?: "Группа: Все группы"

                NeuChip(
                    text = groupLabel,
                    selected = selectedGroup != null,
                    onClick = { groupMenu = true },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = groupMenu,
                    onDismissRequest = { groupMenu = false }
                ) {
                    DropdownMenuItem(onClick = {
                        groupMenu = false
                        onGroupChanged(null)
                    }) {
                        Text("Все группы")
                    }

                    groups.forEach { g ->
                        DropdownMenuItem(onClick = {
                            groupMenu = false
                            onGroupChanged(g)
                        }) {
                            Text(
                                text = g,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = BlueMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun weekDaysOrdered() = listOf(
    WeekDay.Mon, WeekDay.Tue, WeekDay.Wed, WeekDay.Thu, WeekDay.Fri, WeekDay.Sat, WeekDay.Sun
)

private fun shortName(d: WeekDay) = when (d) {
    WeekDay.Mon -> "Пн"
    WeekDay.Tue -> "Вт"
    WeekDay.Wed -> "Ср"
    WeekDay.Thu -> "Чт"
    WeekDay.Fri -> "Пт"
    WeekDay.Sat -> "Сб"
    WeekDay.Sun -> "Вс"
}
