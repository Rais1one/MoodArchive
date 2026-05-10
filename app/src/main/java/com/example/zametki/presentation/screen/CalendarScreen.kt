package com.example.zametki.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zametki.data.local.entity.EntryEntity
import com.example.zametki.presentation.viewmodel.ViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: ViewModel,
    userId: String,
    onBack: () -> Unit,
    onEntryClick: (EntryEntity) -> Unit
) {
    val entries by viewModel.entries.collectAsState()

    val calendar = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val todayMonth = Calendar.getInstance().get(Calendar.MONTH)
    val todayYear = Calendar.getInstance().get(Calendar.YEAR)

    LaunchedEffect(userId) {
        viewModel.loadEntries(userId)
    }

    val selectedDayEntries = remember(entries, selectedDay, currentMonth, currentYear) {
        if (selectedDay == null) emptyList()
        else {
            val cal = Calendar.getInstance()
            cal.set(currentYear, currentMonth, selectedDay!!, 0, 0, 0)
            val startOfDay = cal.timeInMillis
            cal.set(currentYear, currentMonth, selectedDay!!, 23, 59, 59)
            val endOfDay = cal.timeInMillis
            entries.filter { it.createdAt in startOfDay..endOfDay }
        }
    }

    val emotionsByDay = remember(entries, currentMonth, currentYear) {
        val map = mutableMapOf<Int, EntryEntity>()
        entries.forEach { entry ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = entry.createdAt
            if (cal.get(Calendar.MONTH) == currentMonth &&
                cal.get(Calendar.YEAR) == currentYear
            ) {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                if (!map.containsKey(day)) map[day] = entry
            }
        }
        map
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Календарь",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "назад",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentMonth == 0) {
                        currentMonth = 11
                        currentYear--
                    } else {
                        currentMonth--
                    }
                    selectedDay = null
                }) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "предыдущий месяц",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = getMonthName(currentMonth, currentYear),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = {
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear++
                    } else {
                        currentMonth++
                    }
                    selectedDay = null
                }) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "следующий месяц",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEachIndexed { index, day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = if (index >= 5)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val daysInMonth = getDaysInMonth(currentMonth, currentYear)
            val firstDayOfWeek = getFirstDayOfWeek(currentMonth, currentYear)

            val weeks = mutableListOf<List<Int?>>()
            var week = mutableListOf<Int?>()

            repeat(firstDayOfWeek) { week.add(null) }

            for (day in 1..daysInMonth) {
                week.add(day)
                if (week.size == 7) {
                    weeks.add(week)
                    week = mutableListOf()
                }
            }

            if (week.isNotEmpty()) {
                while (week.size < 7) week.add(null)
                weeks.add(week)
            }

            weeks.forEach { weekDays ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekDays.forEach { day ->
                        val isToday = day != null &&
                                day == todayDay &&
                                currentMonth == todayMonth &&
                                currentYear == todayYear
                        val isSelected = day == selectedDay
                        val hasEntry = day != null && emotionsByDay.containsKey(day)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        hasEntry -> parseColor(emotionsByDay[day]!!.emotionColor).copy(alpha = 0.25f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable(enabled = day != null) {
                                    selectedDay = if (selectedDay == day) null else day
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day.toString(),
                                        fontSize = 15.sp,
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onBackground
                                        },
                                        fontWeight = if (isToday || isSelected)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (hasEntry) {
                                        Text(
                                            text = emotionsByDay[day]!!.emotionEmoji,
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedDay != null) {
                Text(
                    text = if (selectedDayEntries.isEmpty())
                        "Нет записей за $selectedDay ${getMonthName(currentMonth, currentYear)}"
                    else
                        "$selectedDay ${getMonthName(currentMonth, currentYear)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedDayEntries.isEmpty())
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(selectedDayEntries) { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEntryClick(entry) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (entry.emotionEmoji.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                parseColor(entry.emotionColor).copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = entry.emotionEmoji, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.size(12.dp))
                                }

                                Column {
                                    if (entry.title.isNotEmpty()) {
                                        Text(
                                            text = entry.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (entry.text.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = entry.text,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getMonthName(month: Int, year: Int): String {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1)
    val sdf = SimpleDateFormat("LLLL yyyy", Locale("ru"))
    return sdf.format(cal.time).replaceFirstChar { it.uppercase() }
}

fun getDaysInMonth(month: Int, year: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1)
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun getFirstDayOfWeek(month: Int, year: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1)
    return (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7
}